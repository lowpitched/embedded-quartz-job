package com.xenia.core;

import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;

@Getter
@Setter
public class Config {

    private DataSource dataSource;

    private String tablePrefix;

}
