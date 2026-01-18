package com.xenia.core.repo;

import com.xenia.core.entity.JobEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class JobEntityRepo {

    private final JdbcTemplate jdbcTemplate;

    private final String tablePrefix;

    private final SqlCollection sqlCollection;

    private final ComplexBeanPropertyRowMapper<JobEntity> rowMapper = ComplexBeanPropertyRowMapper.newInstance(JobEntity.class);

    public JobEntityRepo(DataSource dataSource, String tablePrefix) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.tablePrefix = tablePrefix;
        this.sqlCollection = new SqlCollection(tablePrefix);
    }

    public List<JobEntity> getJobEntities() {
        return jdbcTemplate.query(String.format("select * from %sjob", tablePrefix), rowMapper);
    }

    public Optional<JobEntity> getJobEntity(String jobName, String jobGroup) {
        JobEntity jobEntity = jdbcTemplate.queryForObject(
                String.format("select * from %sjob where name=? and group_name=?", tablePrefix),
                rowMapper,
                jobName,
                jobGroup);
        return Optional.ofNullable(jobEntity);
    }

    public int updateJobEntity(JobEntity jobEntity) {
        List<Object> params = JobEntity.getEntityValues(jobEntity);
        params.add(jobEntity.getName());
        params.add(jobEntity.getGroupName());
        return jdbcTemplate.update(
                sqlCollection.getSqlUpdateJobEntity(),
                params.toArray());
    }

    public JobEntity insertJobEntity(JobEntity jobEntity) {
        List<Object> params = JobEntity.getEntityValues(jobEntity);
        int insert = jdbcTemplate.update(
                sqlCollection.getSqlInsertJobEntity(),
                params.toArray());
        if (insert == 1) {
            return getJobEntity(jobEntity.getName(), jobEntity.getGroupName()).orElse(null);
        }
        return null;
    }

    public int deleteJobEntity(String jobName, String jobGroup) {
        return jdbcTemplate.update(
                sqlCollection.getSqlDeleteJobEntity(),
                jobName,
                jobGroup);
    }

    public int updateJobInstance(String jobName, String groupName, String instanceId, String oldInstanceId) {
        String sql = """
                update
                	%sjob
                set
                	current_instance =?,
                	fire_time = now()
                where
                	name =?
                	and group_name =?
                	and (current_instance =?
                		or (fire_time is null
                			or fire_time + interval '1 second' * expire_seconds < now()))
                """;
        return jdbcTemplate.update(
                String.format(sql, tablePrefix),
                instanceId,
                jobName,
                groupName,
                oldInstanceId
        );
    }


}
