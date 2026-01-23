package com.xenia.core.repo;

import com.xenia.core.entity.JobInstanceEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class JobInstanceEntityRepo {

    private final JdbcTemplate jdbcTemplate;

    private final String tablePrefix;

    private final ComplexBeanPropertyRowMapper<JobInstanceEntity> rowMapper = ComplexBeanPropertyRowMapper.newInstance(JobInstanceEntity.class);

    public JobInstanceEntityRepo(DataSource dataSource, String tablePrefix) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.tablePrefix = tablePrefix;
    }

    public JobInstanceEntity insertJobInstanceEntity(JobInstanceEntity jobInstanceEntity) {
        String sql = """
                INSERT INTO %sjob_instance
                (job_id, instance_id, status, start_time)
                VALUES (?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                String.format(sql, tablePrefix),
                jobInstanceEntity.getJobId(),
                jobInstanceEntity.getInstanceId(),
                jobInstanceEntity.getStatus(),
                jobInstanceEntity.getStartTime()
        );
        return getByJobIdAndInstanceId(jobInstanceEntity.getJobId(), jobInstanceEntity.getInstanceId());
    }

    public int updateStatusByJobIdAndInstanceId(Long jobId,
                                                String instanceId,
                                                String status,
                                                String oldStatus,
                                                String errorMessage) {
        String sql = """
                UPDATE %sjob_instance
                SET status = ?,
                end_time = now(),
                error_message = ?
                WHERE job_id = ?
                AND instance_id = ?
                AND status = ?
                """;
        return jdbcTemplate.update(
                String.format(sql, tablePrefix),
                status,
                errorMessage,
                jobId,
                instanceId,
                oldStatus
        );
    }

    public JobInstanceEntity getByJobIdAndInstanceId(Long jobId, String instanceId) {
        String sql = """
                SELECT * FROM %sjob_instance
                WHERE job_id = ?
                AND instance_id = ?
                """;
        return jdbcTemplate.queryForObject(
                String.format(sql, tablePrefix),
                rowMapper,
                jobId,
                instanceId
        );
    }

}
