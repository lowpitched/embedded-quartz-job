package com.xenia.core.repo;

public class SqlCollection {

    static final String SQL_GET_JOB_ENTITIES = """
            SELECT name, group, clazz, cron, status, params FROM %sjob
            """;
    static final String SQL_UPDATE_JOB_ENTITY = """
            update %sjob set clazz=?, cron=?, status=?, params=? where name=? and group=?
            """;

    static final String SQL_INSERT_JOB_ENTITY = """
            insert into %sjob (name, group, clazz, cron, status, params) values (?, ?, ?, ?, ?, ?)
            """;

    static final String SQL_DELETE_JOB_ENTITY = """
            delete from %sjob where name=? and group=?
            """;

    static final String SQL_GET_JOB_ENTITY = """
            SELECT name, group, clazz, cron, status, params FROM %sjob where name=? and group=?
            """;

    private final String tablePrefix;

    public SqlCollection(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public String getSqlGetJobEntities() {
        return String.format(SQL_GET_JOB_ENTITIES, tablePrefix);
    }


    public String getSqlUpdateJobEntity() {
        return String.format(SQL_UPDATE_JOB_ENTITY, tablePrefix);
    }

    public String getSqlInsertJobEntity() {
        return String.format(SQL_INSERT_JOB_ENTITY, tablePrefix);
    }

    public String getSqlDeleteJobEntity() {
        return String.format(SQL_DELETE_JOB_ENTITY, tablePrefix);
    }

    public String getSqlGetJobEntity() {
        return String.format(SQL_GET_JOB_ENTITY, tablePrefix);
    }
}
