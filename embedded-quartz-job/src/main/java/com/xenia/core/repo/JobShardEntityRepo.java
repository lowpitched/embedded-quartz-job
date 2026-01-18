package com.xenia.core.repo;

import com.xenia.core.entity.JobShardEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;

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
                JobShardEntity.getEntityValues(jobShardEntity).toArray());
    }

    public void updateJobShardEntityStatus(Long id, String status, String oldStatus) {
        jdbcTemplate.update(
                sqlCollection.updateJobShardEntityStatus(),
                status, id, oldStatus);
    }

    public JobShardEntity getJobShardEntity(Long jobId, String instanceId, Integer shardIndex) {
       /* return jdbcTemplate.queryForObject(
                "select * from job_shard where job_id=? and instance_id=? and shard_index=?",
                (rs, rowNum) -> JobShardEntity.builder()
                        .id(rs.getLong("id"))
                        .jobId(rs.getLong("job_id"))
                        .instanceId(rs.getString("instance_id"))
                        .shardIndex(rs.getInt("shard_index"))
                        .status(rs.getString("status"))
                        .startTime(toLocalDateTime(rs.getTimestamp("start_time")))
                        .endTime(toLocalDateTime(rs.getTimestamp("end_time")))
                        .build(), jobId, instanceId, shardIndex);*/
        return jdbcTemplate.queryForObject(
                "select * from job_shard where job_id=? and instance_id=? and shard_index=?",
                rowMapper,
                jobId, instanceId, shardIndex);
    }

    private LocalDateTime toLocalDateTime(java.sql.Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    public JobShardEntity getJobShardEntityById(Long id) {
        return jdbcTemplate.queryForObject(
                sqlCollection.getSqlGetJobShardEntityById(),
                rowMapper,
                id);
    }


}
