package com.xenia.core.repo;

import com.xenia.core.po.JobEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                JobEntity jobEntity = JobEntity.builder()
                        .name(rs.getString("name"))
                        .group(rs.getString("group"))
                        .clazz(Class.forName(rs.getString("clazz")))
                        .cron(rs.getString("cron"))
                        .status(JobEntity.Status.fromString(rs.getString("status")))
                        .params(rs.getObject("params", Map.class))
                        .build();
                jobEntities.add(jobEntity);
            }
        } catch (SQLException | ClassNotFoundException e) {
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
                ps.setString(1, jobEntity.getName());
                ps.setString(2, jobEntity.getGroup());
                ps.setString(3, jobEntity.getClazz().getName());
                ps.setString(4, jobEntity.getCron());
                ps.setString(5, jobEntity.getStatus().name());
                ps.setObject(6, jobEntity.getParams());
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
                    JobEntity jobEntity = JobEntity.builder()
                            .name(rs.getString("name"))
                            .group(rs.getString("group"))
                            .clazz(Class.forName(rs.getString("clazz")))
                            .cron(rs.getString("cron"))
                            .status(JobEntity.Status.fromString(rs.getString("status")))
                            .params(rs.getObject("params", Map.class))
                            .build();
                            return Optional.of(jobEntity);
                }
                return Optional.empty();
            } catch (ClassNotFoundException e) {
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
            ps.setString(1, jobEntity.getName());
            ps.setString(2, jobEntity.getGroup());
            ps.setString(3, jobEntity.getClazz().getName());
            ps.setString(4, jobEntity.getCron());
            ps.setString(5, jobEntity.getStatus().name());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteJobEntity(String jobName, String jobGroup) {
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(
                     sqlCollection.getSqlDeleteJobEntity()
             );
        ) {
            ps.setString(1, jobName);
            ps.setString(2, jobGroup);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConn() throws SQLException {
        return dataSource.getConnection();
    }

}
