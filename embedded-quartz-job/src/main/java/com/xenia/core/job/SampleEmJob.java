package com.xenia.core.job;

import org.quartz.JobExecutionException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class SampleEmJob implements EmJob{

    private Logger logger = Logger.getLogger(SampleEmJob.class.getName());
    @Override
    public void execute(EmJobContext context) throws JobExecutionException {
        logger.info(String.format("""
                        ================================
                        this is a test embedded job
                        jobName: %s
                        jobGroup: %s
                        jobInstance: %s
                        totalShard: %s 
                        shard: %s 
                        thread: %s
                        timestamp: %s
                        """,
                context.getContext().getJobDetail().getKey().getName(),
                context.getContext().getJobDetail().getKey().getGroup(),
                context.getContext().getMergedJobDataMap().get("instanceId"),
                context.getTotalShard(),
                context.getShard(),
                Thread.currentThread().getThreadGroup().getName() + "-" + Thread.currentThread().getName(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
