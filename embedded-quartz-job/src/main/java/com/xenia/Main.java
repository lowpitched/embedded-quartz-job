package com.xenia;

import com.xenia.core.Config;
import com.xenia.core.JobLauncher;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class Main {
    public static void main(String[] args) {
       try {
           Config config = new Config();
           config.setAllowMultiThread(false);
           DriverManagerDataSource dataSource = new DriverManagerDataSource();
           dataSource.setDriverClassName("org.postgresql.Driver");
           dataSource.setUrl("jdbc:postgresql://localhost:5432/postgres?currentSchema=myjobs");
           dataSource.setUsername(args[0]);
           dataSource.setPassword(args[1]);
           config.setDataSource(dataSource);
           config.setTablePrefix("");
           JobLauncher.instance(config).startSchedule();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}