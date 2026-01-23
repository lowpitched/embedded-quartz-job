package com.xenia.core.repo;

import com.xenia.core.entity.JobInstanceShardEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class JobInstanceShardEntityRepo {

    private final JdbcTemplate jdbcTemplate;

    private final String tablePrefix;

    private final SqlCollection sqlCollection;

    private final ComplexBeanPropertyRowMapper<JobInstanceShardEntity> rowMapper = ComplexBeanPropertyRowMapper.newInstance(JobInstanceShardEntity.class);

    public JobInstanceShardEntityRepo(DataSource dataSource, String tablePrefix) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.tablePrefix = tablePrefix;
        this.sqlCollection = new SqlCollection(tablePrefix);
    }

    public void insertJobShardEntity(JobInstanceShardEntity jobInstanceShardEntity) {
        jdbcTemplate.update(
                sqlCollection.getSqlInsertJobShardEntity(),
                JobInstanceShardEntity.getEntityValues(jobInstanceShardEntity).toArray()
        );
    }

    public void updateStatusToCompleted(Long id) {
        String sql = """
                update %sjob_instance_shard set status='COMPLETED', end_time=now() where id=? and status='RUNNING'
                """;
        jdbcTemplate.update(String.format(sql, tablePrefix), id);
    }

    public void updateStatusToFailed(Long id) {
        String sql = """
                update %sjob_instance_shard set status='FAILED', end_time=now(), retry_times=retry_times+1 where id=? and status='RUNNING'
                """;
        jdbcTemplate.update(String.format(sql, tablePrefix), id);
    }

    public JobInstanceShardEntity getJobShardEntity(Long jobId, String instanceId, Integer shardIndex) {
        String sql = """
                select * from %sjob_instance_shard where job_id=? and instance_id=? and shard_index=?
                """;
        return jdbcTemplate.queryForObject(
                String.format(sql, tablePrefix),
                rowMapper,
                jobId,
                instanceId,
                shardIndex
        );
    }

    public JobInstanceShardEntity getJobShardEntityById(Long id) {
        String sql = """
                select * from %sjob_instance_shard where id=?
                """;
        return jdbcTemplate.queryForObject(
                String.format(sql, tablePrefix),
                rowMapper,
                id
        );
    }

    public void updateFailedStatusToRunning(Long id) {
        String sql = """
                update %sjob_instance_shard set status='RUNNING', start_time=now() where id=? and status='FAILED'
                """;
        jdbcTemplate.update(String.format(sql, tablePrefix), id);
    }

    public List<JobInstanceShardEntity> getFailedShards(Long jobId, String instanceId) {
        String sql = """
                select * from %sjob_instance_shard where job_id = ? and instance_id = ? and status = 'FAILED'
                """;
        return jdbcTemplate.query(String.format(sql, tablePrefix), rowMapper, jobId, instanceId);
    }

    public boolean isAllShardsSuccess(Long jobId, String instanceId) {
        String sql = """
                select count(1) from %sjob_instance_shard where job_id = ? and instance_id = ? and status != 'COMPLETED'
                """;
        Integer count = jdbcTemplate.queryForObject(String.format(sql, tablePrefix), Integer.class, jobId, instanceId);
        return count == 0;
    }


}
