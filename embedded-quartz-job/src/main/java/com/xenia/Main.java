package com.xenia;

import com.xenia.core.MyJob;
import com.xenia.core.po.JobEntity;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        try {
            JobEntity jobEntity = JobEntity.builder()
                    .name("jobName")
                    .group("jobGroup")
                    .clazz(MyJob.class)
                    .cron("0/5 * * * * ?")
                    .params(Map.of("param1", "value1"))
                    .build();
            CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("triggerName", "triggerGroup")
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobEntity.getCron()))
                    .build();

            Scheduler scheduler = schedulerFactory.getScheduler();
            JobDetail jobDetail = JobBuilder.newJob()
                    .withIdentity(jobEntity.getName(), jobEntity.getGroup())
                    .ofType(jobEntity.getClazz())
                    .usingJobData(new JobDataMap(jobEntity.getParams()))
                    .build();

            scheduler.scheduleJob(jobDetail, cronTrigger);
            scheduler.start();
            System.out.println("--------end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}