package com.xenia;

import com.xenia.core.Config;
import com.xenia.core.JobContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class Main {
    public static void main(String[] args) {
       try {
           Config config = new Config();
           DriverManagerDataSource dataSource = new DriverManagerDataSource();
           dataSource.setDriverClassName("org.postgresql.Driver");
           dataSource.setUrl("jdbc:postgresql://localhost:5432/postgres?currentSchema=myjobs");
           dataSource.setUsername("****");
           dataSource.setPassword("****");
           config.setDataSource(dataSource);
           config.setTablePrefix("");
           JobContext.instance(config).startSchedule();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}