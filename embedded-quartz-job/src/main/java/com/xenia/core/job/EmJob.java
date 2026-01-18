package com.xenia.core.job;

import org.quartz.JobExecutionException;

public interface EmJob {

    void execute(EJobContext context) throws JobExecutionException;

}
