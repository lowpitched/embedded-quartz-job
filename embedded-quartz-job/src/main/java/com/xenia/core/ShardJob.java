package com.xenia.core;

import com.google.common.collect.Lists;
import com.xenia.config.PrimaryThreadPool;
import com.xenia.core.entity.JobEntity;
import com.xenia.core.entity.JobShardEntity;
import com.xenia.core.job.EJobContext;
import com.xenia.core.job.EmJob;
import com.xenia.core.repo.JobEntityRepo;
import com.xenia.core.repo.JobShardEntityRepo;
import org.quartz.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@DisallowConcurrentExecution
public class ShardJob implements Job {

    private static final Logger logger = Logger.getLogger(ShardJob.class.getName());

    private static JobEntityRepo jobRepo;

    private static JobShardEntityRepo jobShardRepo;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobLauncher jobLauncher = (JobLauncher) context.getMergedJobDataMap().get(JobLauncher.JOB_CONTEXT);
        initRepo(jobLauncher);
        JobKey key = context.getJobDetail().getKey();
        Optional<JobEntity> jobEntityOptional = jobRepo.getJobEntity(key.getName(), key.getGroup());
        if (jobEntityOptional.isEmpty() || jobEntityOptional.get().getStatus() == JobEntity.Status.PAUSED) {
            logger.warning("job " + key.getName() + " group " + key.getGroup() + " has been deleted or paused");
            return;
        }
        JobEntity jobEntity = jobEntityOptional.get();
        updateInstance(jobEntity, UUID.randomUUID().toString());
        context.getMergedJobDataMap().put("instanceId", jobEntity.getCurrentInstance());
        Integer totalShards = jobEntity.getTotalShards();

        Config config = jobLauncher.getConfig();
        ExecutorService executor = PrimaryThreadPool.instance(jobLauncher.getConfig().getThreadPoolConfig()).getExecutor();
        List<JobShardEntity> futureResults = Lists.newArrayList();

        List<Future<JobShardEntity>> futures = Lists.newArrayList();
        List<Integer> shards = generateRandomizedShardIndices(totalShards);
        for (Integer i : shards) {
            try {
                acquireShard(jobEntity, i);
            } catch (Exception e) {
                continue;
            }
            JobShardEntity jobShardEntity = jobShardRepo.getJobShardEntity(jobEntity.getId(), jobEntity.getCurrentInstance(), i);
            int finalI = i;
            Future<JobShardEntity> future = executor.submit(
                    () -> {
                        try {
                            EmJob job = (EmJob) Class.forName(jobEntity.getClazz()).getConstructor().newInstance();
                            EJobContext eJobContext = new EJobContext(context, totalShards, finalI);
                            job.execute(eJobContext);
                            jobShardRepo.updateJobShardEntityStatus(
                                    jobShardEntity.getId(),
                                    JobShardEntity.Status.COMPLETED.name(),
                                    JobShardEntity.Status.RUNNING.name());
                        } catch (Exception e) {
                            jobShardRepo.updateJobShardEntityStatus(
                                    jobShardEntity.getId(),
                                    JobShardEntity.Status.FAILED.name(),
                                    JobShardEntity.Status.RUNNING.name());
                        }
                        return jobShardRepo.getJobShardEntityById(jobShardEntity.getId());
                    });
            if (config.getAllowMultiThread()) {
                futures.add(future);
            } else {
                try {
                    futureResults.add(future.get());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (config.getAllowMultiThread()) {
            for (Future<JobShardEntity> future : futures) {
                try {
                    futureResults.add(future.get());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (JobShardEntity jobShardEntity : futureResults) {
            if (jobShardEntity.getStatus() != JobShardEntity.Status.COMPLETED.name()) {
                //TODO
            }
        }

        jobRepo.updateJobInstance(jobEntity.getName(),
                jobEntity.getGroupName(),
                "0",
                jobEntity.getCurrentInstance());

    }

    private void acquireShard(JobEntity jobEntity, int shardIndex) {
        JobShardEntity jobShardEntity = JobShardEntity.builder()
                .jobId(jobEntity.getId())
                .shardIndex(shardIndex)
                .status(JobShardEntity.Status.RUNNING.name())
                .instanceId(jobEntity.getCurrentInstance())
                .startTime(LocalDateTime.now())
                .build();
        jobShardRepo.insertJobShardEntity(jobShardEntity);
    }

    private void initRepo(JobLauncher jobLauncher) {
        if (jobRepo == null) {
            jobRepo = new JobEntityRepo(
                    jobLauncher.getConfig().getDataSource(),
                    jobLauncher.getConfig().getTablePrefix());
        }
        if (jobShardRepo == null) {
            jobShardRepo = new JobShardEntityRepo(
                    jobLauncher.getConfig().getDataSource(),
                    jobLauncher.getConfig().getTablePrefix());
        }
    }

    private void updateInstance(JobEntity jobEntity, String instanceId) {
        int result = jobRepo.updateJobInstance(jobEntity.getName(), jobEntity.getGroupName(), instanceId, "0");
        if (result == 0) {
            JobEntity entity = jobRepo.getJobEntity(jobEntity.getName(), jobEntity.getGroupName()).orElseThrow(
                    () -> new RuntimeException("job " + jobEntity.getName() + " group " + jobEntity.getGroupName() + " has been deleted")
            );
            jobEntity.setCurrentInstance(entity.getCurrentInstance());
        } else {
            jobEntity.setCurrentInstance(instanceId);
        }
        System.out.println("job instance被更新为：" + jobEntity.getCurrentInstance());
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
