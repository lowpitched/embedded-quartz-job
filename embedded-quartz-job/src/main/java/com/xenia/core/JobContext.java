package com.xenia.core;

import com.xenia.core.job.ShardJob;
import com.xenia.core.po.JobEntity;
import com.xenia.core.repo.BaseRepo;
import lombok.Getter;
import lombok.Setter;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class JobContext {

    private List<JobEntity> jobEntities;

    private Scheduler scheduler;

    private Config config;

    private BaseRepo repo;

    private JobContext() {}

    public static JobContext instance(Config config)
            throws SchedulerException {
        JobContext context = new JobContext();
        context.config = config;
        context.checkConfig();
        context.repo = new BaseRepo(config.getDataSource(), config.getTablePrefix());
        context.scheduler = StdSchedulerFactory.getDefaultScheduler();
        context.jobEntities = context.repo.getJobEntities();
        context.contextPreProcess();
        return context;
    }

    public void startSchedule() throws SchedulerException {
        this.scheduler.start();
    }

    public void shutdownSchedule(boolean waitForJobsToComplete) throws SchedulerException {
        this.scheduler.shutdown(waitForJobsToComplete);
    }

    public void triggerJob(String jobName, String jobGroup) throws SchedulerException {
        this.scheduler.triggerJob(new JobKey(jobName, jobGroup));
    }

    public void addJob(JobEntity jobEntity) {
        this.repo.addJobEntity(jobEntity);
        scheduleJob(jobEntity);
    }

    public void pauseJob(String jobName, String jobGroup) {
        this.repo.getJobEntity(jobName, jobGroup).ifPresent(jobEntity -> {
            jobEntity.setStatus(JobEntity.Status.PAUSED);
            this.repo.updateJobEntity(jobEntity);
            try {
                this.scheduler.pauseJob(new JobKey(jobName, jobGroup));
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void resumeJob(String jobName, String jobGroup) {
        this.repo.getJobEntity(jobName, jobGroup).ifPresentOrElse(jobEntity -> {
            jobEntity.setStatus(JobEntity.Status.NORMAL);
            this.repo.updateJobEntity(jobEntity);
            try {
                this.scheduler.resumeJob(JobKey.jobKey(jobName, jobGroup));
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        }, () -> {throw new RuntimeException();});
    }

    public void refreshJob(String jobName, String jobGroup, String clazz, String cron, Map<String, Object> params) {
        this.repo.getJobEntity(jobName, jobGroup).ifPresentOrElse(jobEntity -> {
            try {
                jobEntity.setParams(params);
                jobEntity.setCron(cron);
                jobEntity.setClazz((Class<? extends Job>) Class.forName(clazz));
                jobEntity.setStatus(JobEntity.Status.NORMAL);
                this.repo.updateJobEntity(jobEntity);
                scheduler.deleteJob(JobKey.jobKey(jobName, jobGroup));
                scheduleJob(jobEntity);
            } catch (ClassNotFoundException | SchedulerException e) {
                throw new RuntimeException(e);
            }
        }, () -> {throw new RuntimeException("error");});
    }

    public void deleteJob(String jobName, String jobGroup) {
        this.repo.getJobEntity(jobName, jobGroup).ifPresent(jobEntity -> {
            try {
                this.scheduler.deleteJob(JobKey.jobKey(jobName, jobGroup));
                this.repo.deleteJobEntity(jobName, jobGroup);
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void contextPreProcess() {
        this.jobEntities.forEach(this::scheduleJob);
    }

    private void scheduleJob(JobEntity jobEntity) {
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity(
                        jobEntity.getName()+"_trigger",
                        jobEntity.getGroup()+"_trigger-group"
                )
                .withSchedule(CronScheduleBuilder.cronSchedule(jobEntity.getCron()))
                .build();
        JobDataMap jobDataMap = new JobDataMap(jobEntity.getParams());
        jobDataMap.put("jobMeta", jobEntity);
        jobDataMap.put("jobContext", this);
        JobDetail jobDetail = JobBuilder.newJob()
                .withIdentity(jobEntity.getName(), jobEntity.getGroup())
                .ofType(ShardJob.class)
                .usingJobData(jobDataMap)
                .build();
        try {
            this.scheduler.scheduleJob(jobDetail, cronTrigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkConfig() {
        assert Objects.nonNull(config.getDataSource()) : "not config dataSource";
        //TODO
    }

}
