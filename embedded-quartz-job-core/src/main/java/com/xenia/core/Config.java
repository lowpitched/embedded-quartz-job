package com.xenia.core;

import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;

@Getter
@Setter
public class Config {

    private DataSource dataSource;

    private String tablePrefix = "";

    private Boolean allowMultiThread = Boolean.FALSE;

    private Integer retryTimes = 3;

    private Integer scanIntervalSeconds = 5;

    private ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig();

    @Getter
    @Setter
    public static class ThreadPoolConfig {

        private int corePoolSize = 5;

        private int maxPoolSize = 10;

        private int queueCapacity = 10000;

        private int keepAliveSeconds = 60;

        private String threadNamePrefix = "Xenia-ThreadPool-";

        private String threadPoolName = "Xenia-ThreadPool";
    }

}
