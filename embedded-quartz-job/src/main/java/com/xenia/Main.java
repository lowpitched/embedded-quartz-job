package com.xenia;

import com.xenia.core.Config;
import com.xenia.core.JobContext;

public class Main {
    public static void main(String[] args) {
       try {
           Config config = new Config();
           //config.setDataSource(new DataSource());
           config.setTablePrefix("");
           JobContext.instance(new Config()).startSchedule();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}