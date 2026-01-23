package com.xenia.core.repo;

import com.xenia.core.entity.BaseEntity;
import com.xenia.core.entity.JobEntity;
import com.xenia.core.entity.JobInstanceShardEntity;

import java.util.List;

public class SqlCollection {

    static final String SQL_GET_ENTITIES_TEMPLATE = """
            SELECT %s FROM %s
            """;
    static final String SQL_UPDATE_ENTITY_TEMPLATE = """
            update %s set %s where name=? and group_name=?
            """;

    static final String SQL_INSERT_ENTITY_TEMPLATE = """
            insert into %s (%s) values (%s)
            """;

    static final String SQL_DELETE_ENTITY_TEMPLATE = """
            delete from %s where name=? and group_name=?
            """;

    static final String SQL_GET_ENTITY_TEMPLATE = """
            SELECT %s FROM %s where name=? and group_name=?
            """;

    static final String SQL_GET_JOB_SHARD_ENTITY_TEMPLATE = """
            SELECT %s FROM %s where job_id=? and instance_id=? and shard_index=?
            """;

    static final String SQL_GET_BY_ID_TEMPLATE = """
            SELECT %s FROM %s where id=?
            """;

    static final String SQL_UPDATE_JOB_INSTANCE_IF_IS_BLANK_TEMPLATE = """
            update %s set current_instance=? where name=? and group_name=? and current_instance =?
            """;

    static final String SQL_UPDATE_STATUS_TEMPLATE = """
            update %s set status=? where id=? and status=?
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

    public String getSqlUpdateJobInstanceIfisBlank() {
        return String.format(
                SQL_UPDATE_JOB_INSTANCE_IF_IS_BLANK_TEMPLATE,
                tablePrefix + "job"
        );
    }

    public String getSqlInsertJobShardEntity() {
        return String.format(
                SQL_INSERT_ENTITY_TEMPLATE,
                tablePrefix + "job_instance_shard",
                String.join(", ", JobInstanceShardEntity.getColumnNameToField(JobInstanceShardEntity.class).keySet()),
                String.join(", ", JobInstanceShardEntity.getColumnNameToField(JobInstanceShardEntity.class).keySet().stream().map(column -> "?").toList())
        );
    }

    public String updateJobShardEntityStatus() {
        return String.format(
                SQL_UPDATE_STATUS_TEMPLATE,
                tablePrefix + "job_instance_shard"
        );
    }

    public String getSqlGetJobShardEntity() {
        return String.format(
                SQL_GET_JOB_SHARD_ENTITY_TEMPLATE,
                String.join(", ", JobInstanceShardEntity.getColumnNameToField(JobInstanceShardEntity.class).keySet()),
                tablePrefix + "job_instance_shard"
        );
    }

    public String getSqlGetJobShardEntityById() {
        return String.format(
                SQL_GET_BY_ID_TEMPLATE,
                String.join(", ", JobInstanceShardEntity.getColumnNameToField(JobInstanceShardEntity.class).keySet()),
                tablePrefix + "job_instance_shard"
        );
    }
}
