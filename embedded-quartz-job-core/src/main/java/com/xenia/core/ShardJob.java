package com.xenia.core;

import com.google.common.collect.Lists;
import com.xenia.config.PrimaryThreadPool;
import com.xenia.core.entity.JobEntity;
import com.xenia.core.entity.JobInstanceEntity;
import com.xenia.core.entity.JobInstanceShardEntity;
import com.xenia.core.job.EmJob;
import com.xenia.core.job.EmJobContext;
import com.xenia.core.repo.JobEntityRepo;
import com.xenia.core.repo.JobInstanceEntityRepo;
import com.xenia.core.repo.JobInstanceShardEntityRepo;
import org.quartz.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@DisallowConcurrentExecution
public class ShardJob implements Job {

    private static final Logger logger = Logger.getLogger(ShardJob.class.getName());

    private static JobEntityRepo jobRepo;

    private static JobInstanceShardEntityRepo jobShardRepo;

    private static JobInstanceEntityRepo jobInstanceRepo;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        initRepo((JobLauncher) context.getMergedJobDataMap().get(JobLauncher.JOB_CONTEXT));
        JobKey key = context.getJobDetail().getKey();
        JobEntity jobEntity = jobRepo.getJobEntity(key.getName(), key.getGroup()).orElseThrow();
        boolean isMaster = updateJobCurrentInstance(jobEntity, UUID.randomUUID().toString());
        if (isMaster) {
            logger.info("-------master thread: " + Thread.currentThread().getName());
        }
        context.getMergedJobDataMap().put("instanceId", jobEntity.getCurrentInstance());
        List<Integer> shards = generateRandomizedShardIndices(jobEntity.getTotalShards());
        processJobShard(context, jobEntity, shards, isMaster);
    }

    private void processJobShard(JobExecutionContext context,
                                 JobEntity jobEntity,
                                 List<Integer> shards,
                                 Boolean isMaster) {
        JobLauncher jobLauncher = (JobLauncher) context.getMergedJobDataMap().get(JobLauncher.JOB_CONTEXT);
        Config config = jobLauncher.getConfig();
        ExecutorService executor = PrimaryThreadPool.instance(config.getThreadPoolConfig()).getExecutor();
        List<Future<JobInstanceShardEntity>> futures = Lists.newArrayList();
        for (Integer i : shards) {
            if (!acquireShard(jobEntity, i)) {
                continue;
            }
            JobInstanceShardEntity jobInstanceShardEntity = jobShardRepo
                    .getJobShardEntity(jobEntity.getId(), jobEntity.getCurrentInstance(), i);
            int finalI = i;
            Future<JobInstanceShardEntity> future = executor.submit(
                    () -> processJobShardForOne(
                            context,
                            jobEntity,
                            jobInstanceShardEntity,
                            jobEntity.getTotalShards(),
                            finalI));
            if (config.getAllowMultiThread()) {
                futures.add(future);
            } else {
                this.getFuture(future);
            }
        }
        if (config.getAllowMultiThread()) {
            futures.forEach(this::getFuture);
        }
        if (isMaster) {
            closeJobInstance(context, jobEntity);
        }
    }


    private void getFuture(Future<?> future) {
        try {
            future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JobInstanceShardEntity processJobShardForOne(JobExecutionContext context,
                                                         JobEntity jobEntity,
                                                         JobInstanceShardEntity shardEntity,
                                                         Integer totalShards,
                                                         Integer shardIndex) {
        try {
            EmJob job = (EmJob) Class.forName(jobEntity.getClazz()).getConstructor().newInstance();
            EmJobContext emJobContext = new EmJobContext(context, totalShards, shardIndex);
            job.execute(emJobContext);
            jobShardRepo.updateStatusToCompleted(shardEntity.getId());
        } catch (Exception e) {
            jobShardRepo.updateStatusToFailed(shardEntity.getId());
        }
        return jobShardRepo.getJobShardEntityById(shardEntity.getId());
    }

    private void closeJobInstance(JobExecutionContext context, JobEntity jobEntity) {

        final AtomicReference<ScheduledFuture<?>> futureRef = new AtomicReference<>();
        JobLauncher jobLauncher = (JobLauncher) context.getMergedJobDataMap().get(JobLauncher.JOB_CONTEXT);
        Config config = jobLauncher.getConfig();

        ScheduledFuture<?> future = PrimaryThreadPool.instance(config.getThreadPoolConfig())
                .getScheduledExecutor().scheduleAtFixedRate(() -> {
                    if (jobShardRepo.isAllShardsSuccess(jobEntity.getId(), jobEntity.getCurrentInstance())) {
                        jobInstanceRepo.updateStatusByJobIdAndInstanceId(
                                jobEntity.getId(),
                                jobEntity.getCurrentInstance(),
                                JobInstanceEntity.Status.COMPLETED.name(),
                                JobInstanceEntity.Status.RUNNING.name(),
                                "success");
                        jobRepo.updateJobInstance(jobEntity.getName(),
                                jobEntity.getGroupName(),
                                "0",
                                jobEntity.getCurrentInstance());
                        logger.info("-------master thread - " + Thread.currentThread().getName() + " close job instance");
                        ScheduledFuture<?> currentFuture = futureRef.get();
                        if (currentFuture != null) {
                            currentFuture.cancel(false);
                        }
                    } else {
                        // master兜底失败分片
                        List<JobInstanceShardEntity> failedShards = jobShardRepo
                                .getFailedShards(jobEntity.getId(), jobEntity.getCurrentInstance());

                        boolean finalFailed = failedShards.stream()
                                .anyMatch(shard -> shard.getRetryTimes() >= config.getRetryTimes());
                        if (finalFailed) {
                            jobInstanceRepo.updateStatusByJobIdAndInstanceId(
                                    jobEntity.getId(),
                                    jobEntity.getCurrentInstance(),
                                    JobInstanceEntity.Status.FAILED.name(),
                                    JobInstanceEntity.Status.RUNNING.name(),
                                    "failed");
                            ScheduledFuture<?> currentFuture = futureRef.get();
                            if (currentFuture != null) {
                                currentFuture.cancel(false);
                            }
                        }
                        List<Integer> failedShardIdx = failedShards.stream().map(JobInstanceShardEntity::getShardIndex).toList();
                        failedShards.forEach(shard -> jobShardRepo.updateFailedStatusToRunning(shard.getId()));
                        processJobShard(context, jobEntity, failedShardIdx, true);
                    }
                }, 0, config.getScanIntervalSeconds(), TimeUnit.SECONDS);
        futureRef.set(future);
        try {
            if (!future.isCancelled() && !future.isDone()) {
                future.get(jobEntity.getExpireSeconds(), TimeUnit.SECONDS);
            }
        } catch (CancellationException e) {
            logger.info("Job was cancelled: " + jobEntity.getName());
        } catch (Exception e) {
            logger.warning("Job timeout, shutting down scheduler for job: " + jobEntity.getName());
            ScheduledFuture<?> currentFuture = futureRef.get();
            if (currentFuture != null) {
                currentFuture.cancel(true);
            }
        }
    }


    private boolean acquireShard(JobEntity jobEntity, int shardIndex) {
        try {
            JobInstanceShardEntity jobInstanceShardEntity = JobInstanceShardEntity.builder()
                    .jobId(jobEntity.getId())
                    .shardIndex(shardIndex)
                    .status(JobInstanceShardEntity.Status.RUNNING.name())
                    .instanceId(jobEntity.getCurrentInstance())
                    .startTime(LocalDateTime.now())
                    .build();
            jobShardRepo.insertJobShardEntity(jobInstanceShardEntity);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    private void initRepo(JobLauncher jobLauncher) {
        if (jobRepo == null) {
            jobRepo = new JobEntityRepo(
                    jobLauncher.getConfig().getDataSource(),
                    jobLauncher.getConfig().getTablePrefix());
        }
        if (jobShardRepo == null) {
            jobShardRepo = new JobInstanceShardEntityRepo(
                    jobLauncher.getConfig().getDataSource(),
                    jobLauncher.getConfig().getTablePrefix());
        }
        if (jobInstanceRepo == null) {
            jobInstanceRepo = new JobInstanceEntityRepo(
                    jobLauncher.getConfig().getDataSource(),
                    jobLauncher.getConfig().getTablePrefix());
        }
    }

    private boolean updateJobCurrentInstance(JobEntity jobEntity, String instanceId) {
        int result = jobRepo.updateJobInstance(jobEntity.getName(),
                jobEntity.getGroupName(), instanceId, "0");
        boolean isMaster = false;
        if (result == 0) {
            JobEntity entity = jobRepo.getJobEntity(
                            jobEntity.getName(),
                            jobEntity.getGroupName())
                    .orElseThrow(() -> new RuntimeException(String.format(
                            "job %s group %s has been deleted",
                            jobEntity.getName(),
                            jobEntity.getGroupName())));
            jobEntity.setCurrentInstance(entity.getCurrentInstance());
        } else {
            jobEntity.setCurrentInstance(instanceId);
            jobInstanceRepo.insertJobInstanceEntity(JobInstanceEntity.builder()
                    .jobId(jobEntity.getId())
                    .instanceId(jobEntity.getCurrentInstance())
                    .status(JobInstanceEntity.Status.RUNNING.name())
                    .startTime(LocalDateTime.now())
                    .build());
            isMaster = true;
        }
        return isMaster;
    }

    private List<Integer> generateRandomizedShardIndices(int totalShards) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 1; i <= totalShards; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);
        return indices;
    }
}
