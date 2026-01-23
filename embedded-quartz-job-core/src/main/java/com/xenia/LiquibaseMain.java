package com.xenia;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

public class LiquibaseMain {

    public static void main(String[] args) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                new JdbcConnection(getConnection(args).getConnection())
        );
        Liquibase liquibase = new Liquibase(
                "db/changelog/changelog-master-postgresql.sql",
                new ClassLoaderResourceAccessor(
                        LiquibaseMain.class.getClassLoader()
                ),
                database
        );
        liquibase.setChangeLogParameter("tablePrefix", "test_");
        liquibase.update("");
    }

    public static DriverManagerDataSource getConnection(String[] args) throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/postgres?currentSchema=myjobs");
        dataSource.setUsername(args[0]);
        dataSource.setPassword(args[1]);
        return dataSource;
    }

}
