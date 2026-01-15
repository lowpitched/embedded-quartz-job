package com.xenia.core.repo;

import com.xenia.core.po.BaseEntity;
import com.xenia.core.po.JobEntity;
import com.xenia.util.JsonUtil;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;

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
                List<Object> values = JobEntity.getEntityValues(jobEntity);
                values.addAll(List.of(jobEntity.getName(), jobEntity.getGroupName()));
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
            List<Object> values = JobEntity.getEntityValues(jobEntity);
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
        LinkedHashMap<String, Field> columnFields = entity.getColumnNameToField();
        for (Map.Entry<String, Field> columnField : columnFields.entrySet()) {
            Object value = rs.getObject(columnField.getKey());
            columnField.getValue().setAccessible(true);
            if (value == null) {
                continue;
            }
            if (columnField.getValue().getType().isEnum()) {
                columnField.getValue().set(
                        entity,
                        Enum.valueOf(
                                (Class<? extends Enum>) columnField.getValue().getType(),
                                value.toString())
                );
            } else if (columnField.getValue().getType().isAssignableFrom(LocalDateTime.class)) {
                columnField.getValue().set(entity, rs.getTimestamp(columnField.getKey()).toLocalDateTime());
            } else if (columnField.getValue().getType().isAssignableFrom(String.class)) {
                columnField.getValue().set(entity, value.toString());
            } else if (columnField.getValue().getType().isAssignableFrom(Integer.class)) {
                columnField.getValue().set(entity, rs.getInt(columnField.getKey()));
            } else if (columnField.getValue().getType().isAssignableFrom(Long.class)) {
                columnField.getValue().set(entity, rs.getLong(columnField.getKey()));
            } else if (columnField.getValue().getType().isAssignableFrom(Boolean.class)) {
                columnField.getValue().set(entity, rs.getBoolean(columnField.getKey()));
            } else if (columnField.getValue().getType().isAssignableFrom(Double.class)) {
                columnField.getValue().set(entity, rs.getDouble(columnField.getKey()));
            } else if (columnField.getValue().getType().isAssignableFrom(Float.class)) {
                columnField.getValue().set(entity, rs.getFloat(columnField.getKey()));
            } else if (columnField.getValue().getType().isAssignableFrom(Byte.class)) {
                columnField.getValue().set(entity, rs.getByte(columnField.getKey()));
            } else if (columnField.getValue().getType().isAssignableFrom(Short.class)) {
                columnField.getValue().set(entity, rs.getShort(columnField.getKey()));
            } else if (columnField.getValue().getType().isAssignableFrom(BigDecimal.class)) {
                columnField.getValue().set(entity, rs.getBigDecimal(columnField.getKey()));
            } else if (columnField.getValue().getType().isAssignableFrom(BigInteger.class)) {
                columnField.getValue().set(entity, rs.getBigDecimal(columnField.getKey()).toBigInteger());
            } else if (columnField.getValue().getType().isAssignableFrom(Date.class)) {
                columnField.getValue().set(entity, rs.getDate(columnField.getKey()));
            } else if (columnField.getValue().getType().isAssignableFrom(Time.class)) {
                columnField.getValue().set(entity, rs.getTime(columnField.getKey()));
            } else if (columnField.getValue().getType().isAssignableFrom(Timestamp.class)) {
                columnField.getValue().set(entity, rs.getTimestamp(columnField.getKey()));
            } else if (columnField.getValue().getType().isAssignableFrom(Map.class)) {
                columnField.getValue().set(entity, JsonUtil.fromJson(rs.getString(columnField.getKey()), Map.class));
            } else {
                throw new RuntimeException("Unsupported type: " + columnField.getValue().getType());
            }
        }
        return entity;
    }

    private Connection getConn() throws SQLException {
        return dataSource.getConnection();
    }

}
