package com.xenia.core.repo;

import com.xenia.core.po.BaseEntity;
import com.xenia.core.po.JobEntity;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BaseRepo {

    private final SqlCollection sqlCollection;

    private final DataSource dataSource;

    public BaseRepo(DataSource dataSource, String tablePrefix) {
        this.dataSource = dataSource;
        this.sqlCollection = new SqlCollection(tablePrefix);
    }

    public List<JobEntity> getJobEntities() {
        List<JobEntity> jobEntities = new ArrayList<>();
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(
                     sqlCollection.getSqlGetJobEntities()
             );
             ResultSet rs = ps.executeQuery();
        ) {
            while (rs.next()) {
                jobEntities.add(mapToEntity(rs, JobEntity.class));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return jobEntities;
    }

    public void updateJobEntity(JobEntity jobEntity) {
        try {
            try (Connection conn = getConn();
                 PreparedStatement ps = conn.prepareStatement(
                         sqlCollection.getSqlUpdateJobEntity()
                 );
            ) {
                List<Object> values = JobEntity.getColumnValues(jobEntity);
                values.addAll(List.of(jobEntity.getName(), jobEntity.getGroup()));
                values.forEach(value -> {
                    try {
                        ps.setObject(values.indexOf(value) + 1, value);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<JobEntity> getJobEntity(String jobName, String jobGroup) {
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(
                     sqlCollection.getSqlGetJobEntity()
             );
        ) {
            ps.setString(1, jobName);
            ps.setString(2, jobGroup);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToEntity(rs, JobEntity.class));
                }
                return Optional.empty();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addJobEntity(JobEntity jobEntity) {
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(
                     sqlCollection.getSqlInsertJobEntity()
             );
        ) {
            List<Object> values = JobEntity.getColumnValues(jobEntity);
            for (Object value : values) {
                ps.setObject(values.indexOf(value) + 1, value);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteJobEntity(String jobName, String jobGroup) {
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(
                     sqlCollection.getSqlDeleteJobEntity()
             )
        ) {
            ps.setString(1, jobName);
            ps.setString(2, jobGroup);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends BaseEntity> T mapToEntity(ResultSet rs, Class<T> clazz) throws Exception {
        T entity = clazz.getConstructor().newInstance();
        List<String> columnNames = entity.getColumnNames();
        for (String columnName : columnNames) {
            Object value = rs.getObject(columnName);
            Field field = entity.getClass().getField(columnName);
            field.setAccessible(true);
            field.set(entity, value);
        }
        return entity;
    }

    private Connection getConn() throws SQLException {
        return dataSource.getConnection();
    }

}
