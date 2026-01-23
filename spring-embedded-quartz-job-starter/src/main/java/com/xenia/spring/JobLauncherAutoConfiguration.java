package com.xenia.spring;

import com.xenia.core.Config;
import com.xenia.core.JobLauncher;
import com.xenia.spring.config.JobLauncherConfig;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(JobLauncherConfig.class)
public class JobLauncherAutoConfiguration {

    @DependsOn("embeddedJobLiquibase")
    @Bean
    @ConditionalOnMissingBean
    public JobLauncher jobLauncherAutoConfiguration(JobLauncherConfig launcherConfig, DataSource dataSource) throws SchedulerException {
        if (dataSource == null) {
            throw new RuntimeException("DataSource is null");
        }
        Config config = new Config();
        if (StringUtils.isNotBlank(launcherConfig.getTablePrefix())) {
            config.setTablePrefix(launcherConfig.getTablePrefix());
        }
        if (launcherConfig.getAllowMultiThread() != null) {
            config.setAllowMultiThread(launcherConfig.getAllowMultiThread());
        }
        if (launcherConfig.getRetryTimes() != null) {
            config.setRetryTimes(launcherConfig.getRetryTimes());
        }
        if (launcherConfig.getScanIntervalSeconds() != null) {
            config.setScanIntervalSeconds(launcherConfig.getScanIntervalSeconds());
        }
        Config.ThreadPoolConfig threadPoolConfig = new Config.ThreadPoolConfig();
        if (launcherConfig.getThreadPoolConfig() != null) {
            if (launcherConfig.getThreadPoolConfig().getCorePoolSize() != 0) {
                threadPoolConfig.setCorePoolSize(launcherConfig.getThreadPoolConfig().getCorePoolSize());
            }
            if (launcherConfig.getThreadPoolConfig().getMaxPoolSize() != 0) {
                threadPoolConfig.setMaxPoolSize(launcherConfig.getThreadPoolConfig().getMaxPoolSize());
            }
            if (launcherConfig.getThreadPoolConfig().getQueueCapacity() != 0) {
                threadPoolConfig.setQueueCapacity(launcherConfig.getThreadPoolConfig().getQueueCapacity());
            }
            if (launcherConfig.getThreadPoolConfig().getKeepAliveSeconds() != 0) {
                threadPoolConfig.setKeepAliveSeconds(launcherConfig.getThreadPoolConfig().getKeepAliveSeconds());
            }
            if (StringUtils.isNotBlank(launcherConfig.getThreadPoolConfig().getThreadNamePrefix())) {
                threadPoolConfig.setThreadNamePrefix(launcherConfig.getThreadPoolConfig().getThreadNamePrefix());
            }
            if (StringUtils.isNotBlank(launcherConfig.getThreadPoolConfig().getThreadPoolName())) {
                threadPoolConfig.setThreadPoolName(launcherConfig.getThreadPoolConfig().getThreadPoolName());
            }
        }
        config.setThreadPoolConfig(threadPoolConfig);
        config.setDataSource(dataSource);
        JobLauncher instance = JobLauncher.instance(config);
        instance.startSchedule();
        return instance;
    }

    @Bean("embeddedJobLiquibase")
    @ConditionalOnProperties(
            @ConditionalOnProperty(
                    havingValue = "true",
                    value = "job.launcher.liquibase-config.enabled"
            )
    )
    public Liquibase liquibase(JobLauncherConfig config, DataSource dataSource) throws Exception {
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));
        String changelogFile = "";
        if (StringUtils.equalsIgnoreCase("postgresql", config.getLiquibaseConfig().getDatabasePlatform())) {
            changelogFile = "db/changelog/changelog-master-postgresql.sql";
        } else {
            throw new RuntimeException("Unsupported database platform: " + config.getLiquibaseConfig().getDatabasePlatform());
        }
        Liquibase liquibase = new Liquibase(
                changelogFile,
                new ClassLoaderResourceAccessor(this.getClass().getClassLoader()),
                database);
        liquibase.setChangeLogParameter("tablePrefix", config.getTablePrefix());
        liquibase.update("");
        return liquibase;
    }

}
