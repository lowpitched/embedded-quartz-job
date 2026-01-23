package com.xenia.core.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.xenia.util.JsonUtils;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

public class ComplexBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {
    // 指定要转换的实体类类型
    private final Class<T> mappedClass;

    public ComplexBeanPropertyRowMapper(Class<T> mappedClass) {
        super(mappedClass);
        this.mappedClass = mappedClass;
    }

    // 静态工厂方法，方便创建实例
    public static <T> ComplexBeanPropertyRowMapper<T> newInstance(Class<T> mappedClass) {
        return new ComplexBeanPropertyRowMapper<>(mappedClass);
    }

    @Nullable
    @Override
    protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
        // 获取字段值
        Object value = JdbcUtils.getResultSetValue(rs, index);

        // 如果是 PGobject 类型（PostgreSQL 自定义类型，包含 jsonb）
        if (value instanceof PGobject) {
            PGobject pgObject = (PGobject) value;
            // 判断类型是否为 jsonb
            if ("jsonb".equals(pgObject.getType())) {
                try {
                    // 将 jsonb 字符串转成 Map
                    return JsonUtils.OBJECTMAPPER.readValue(pgObject.getValue(), new TypeReference<Map<String, Object>>() {});
                } catch (Exception e) {
                    throw new SQLException("解析 JSONB 字段失败", e);
                }
            }
        }
        // LocalDateTime 处理
        if (pd.getPropertyType().equals(LocalDateTime.class)) {
            Timestamp timestamp = rs.getTimestamp(index);
            if (timestamp == null) {
                return null;
            }
            return rs.getTimestamp(index).toLocalDateTime();
        }

        // 非 jsonb 类型，走父类默认逻辑
        return super.getColumnValue(rs, index, pd);
    }

    @Override
    protected Object getColumnValue(ResultSet rs, int index, Class<?> paramType) throws SQLException {
        // 获取字段值
        Object value = JdbcUtils.getResultSetValue(rs, index);

        // 如果是 PGobject 类型（PostgreSQL 自定义类型，包含 jsonb）
        if (value instanceof PGobject) {
            PGobject pgObject = (PGobject) value;
            // 判断类型是否为 jsonb
            if ("jsonb".equals(pgObject.getType())) {
                try {
                    // 将 jsonb 字符串转成 Map
                    return JsonUtils.OBJECTMAPPER.readValue(pgObject.getValue(), new TypeReference<Map<String, Object>>() {});
                } catch (Exception e) {
                    throw new SQLException("解析 JSONB 字段失败", e);
                }
            }
        }
        if (paramType.equals(LocalDateTime.class)) {
            Timestamp timestamp = rs.getTimestamp(index);
            if (timestamp == null) {
                return null;
            }
            return rs.getTimestamp(index).toLocalDateTime();
        }
        // 非 jsonb 类型，走父类默认逻辑
        return super.getColumnValue(rs, index, paramType);
    }
}