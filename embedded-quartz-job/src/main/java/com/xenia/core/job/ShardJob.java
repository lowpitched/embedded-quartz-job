package com.xenia.core.job;

import com.xenia.core.JobContext;
import com.xenia.core.po.JobEntity;
import com.xenia.core.repo.BaseRepo;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

public class ShardJob implements Job {

    private BaseRepo baseRepo;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobContext jobContext = (JobContext)context.getMergedJobDataMap().get("jobContext");
        baseRepo = new BaseRepo(jobContext.getConfig().getDataSource(), jobContext.getConfig().getTablePrefix());

        List<JobEntity> jobEntities = baseRepo.getJobEntities();
        System.out.println(jobEntities);
        String fireInstanceId = context.getFireInstanceId();
        System.out.println(fireInstanceId);
    }
}
