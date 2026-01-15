package com.xenia.core.repo;

import com.xenia.core.po.BaseEntity;
import com.xenia.core.po.JobEntity;

import java.util.List;

public class SqlCollection {

    static final String SQL_GET_ENTITIES_TEMPLATE = """
            SELECT %s FROM %s
            """;
    static final String SQL_UPDATE_ENTITY_TEMPLATE = """
            update %s set %s where name=? and group=?
            """;

    static final String SQL_INSERT_ENTITY_TEMPLATE = """
            insert into %s (%s) values (%s)
            """;

    static final String SQL_DELETE_ENTITY_TEMPLATE = """
            delete from %s where name=? and group=?
            """;

    static final String SQL_GET_ENTITY_TEMPLATE = """
            SELECT %s FROM %s where name=? and group=?
            """;

    private final String tablePrefix;

    private final List<String> jobEntityColumns = BaseEntity.getColumnNameToField(JobEntity.class).keySet().stream().toList();

    public SqlCollection(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public String getSqlGetJobEntities() {
        return String.format(
                SQL_GET_ENTITIES_TEMPLATE,
                String.join(", ", jobEntityColumns),
                tablePrefix + "job"
        );
    }


    public String getSqlUpdateJobEntity() {
        return String.format(
                SQL_UPDATE_ENTITY_TEMPLATE,
                tablePrefix + "job",
                String.join(", ", jobEntityColumns.stream().map(column -> column + "=?").toList())
        );
    }

    public String getSqlInsertJobEntity() {
        return String.format(
                SQL_INSERT_ENTITY_TEMPLATE,
                tablePrefix + "job",
                String.join(", ", jobEntityColumns),
                String.join(", ", jobEntityColumns.stream().map(column -> "?").toList())
        );
    }

    public String getSqlDeleteJobEntity() {
        return String.format(SQL_DELETE_ENTITY_TEMPLATE, tablePrefix + "job");
    }

    public String getSqlGetJobEntity() {
        return String.format(
                SQL_GET_ENTITY_TEMPLATE,
                String.join(", ", jobEntityColumns),
                tablePrefix + "job"
        );
    }


}
