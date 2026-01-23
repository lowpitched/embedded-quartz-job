package com.xenia.core.job;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.quartz.JobExecutionContext;

@Getter
@Setter
@AllArgsConstructor
public class EmJobContext {

    private JobExecutionContext context;
    private int totalShard;
    private int shard;

}
