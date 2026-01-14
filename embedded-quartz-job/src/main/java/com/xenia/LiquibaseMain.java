package com.xenia;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.SQLException;

public class LiquibaseMain {

    public static void main(String[] args) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                new JdbcConnection(getConnection())
        );
        Liquibase liquibase = new Liquibase(
                "db/changelog/changelog-master.sql",
                new ClassLoaderResourceAccessor(
                        LiquibaseMain.class.getClassLoader()
                ),
                database
        );
        liquibase.setChangeLogParameter("tablePrefix", "");
        liquibase.update("");

    }

    public static Connection getConnection() throws SQLException {
        return null;
    }

}
