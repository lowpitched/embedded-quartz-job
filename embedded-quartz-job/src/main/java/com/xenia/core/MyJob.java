package com.xenia.core;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MyJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("--------MyJob");
        System.out.println(jobExecutionContext.getJobDetail().getJobDataMap());
        System.out.println(jobExecutionContext.getMergedJobDataMap());
    }
}
