package com.xenia.core.repo;

import com.xenia.core.entity.JobShardEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class JobShardEntityRepo {

    private final JdbcTemplate jdbcTemplate;

    private final String tablePrefix;

    private final SqlCollection sqlCollection;

    private final BeanPropertyRowMapper<JobShardEntity> rowMapper = BeanPropertyRowMapper.newInstance(JobShardEntity.class);

    public JobShardEntityRepo(DataSource dataSource, String tablePrefix) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.tablePrefix = tablePrefix;
        this.sqlCollection = new SqlCollection(tablePrefix);
    }

    public void insertJobShardEntity(JobShardEntity jobShardEntity) {
        jdbcTemplate.update(
                sqlCollection.getSqlInsertJobShardEntity(),
                JobShardEntity.getEntityValues(jobShardEntity).toArray()
        );
    }

    public void updateJobShardEntityStatus(Long id, String status, String oldStatus) {
        String sql = """
                update %sjob_shard set status=?, end_time=now() where id=? and status=?
                """;
        jdbcTemplate.update(
                String.format(sql, tablePrefix),
                status,
                id,
                oldStatus
        );
    }

    public JobShardEntity getJobShardEntity(Long jobId, String instanceId, Integer shardIndex) {
        String sql = """
                select * from %sjob_shard where job_id=? and instance_id=? and shard_index=?
                """;
        return jdbcTemplate.queryForObject(
                String.format(sql, tablePrefix),
                rowMapper,
                jobId,
                instanceId,
                shardIndex
        );
    }

    public JobShardEntity getJobShardEntityById(Long id) {
        String sql = """
                select * from %sjob_shard where id=?
                """;
        return jdbcTemplate.queryForObject(
                String.format(sql, tablePrefix),
                rowMapper,
                id
        );
    }


}
