package com.xenia.spring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "job.launcher")
public class JobLauncherConfig {

    private String tablePrefix;

    private Boolean allowMultiThread;

    private Integer retryTimes;

    private Integer scanIntervalSeconds;

    private ThreadPoolConfig threadPoolConfig;

    private LiquibaseConfig liquibaseConfig;

    @Getter
    @Setter
    public static class LiquibaseConfig {

        private Boolean enabled;

        private String databasePlatform;

    }

    @Setter
    @Getter
    public static class ThreadPoolConfig {

        private int corePoolSize;

        private int maxPoolSize;

        private int queueCapacity;

        private int keepAliveSeconds;

        private String threadNamePrefix;

        private String threadPoolName;
    }

}
