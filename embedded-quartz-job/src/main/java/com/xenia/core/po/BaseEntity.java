package com.xenia.core.po;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

@Getter
@Setter
public class BaseEntity {

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("created_by")
    private String createdBy;

    @Column("updated_by")
    private String updatedBy;

    @AccessIgnore
    private List< Field> columnNames = getEntityFields(this.getClass());

    @AccessIgnore
    private LinkedHashMap<String, Field> columnNameToField = getColumnNameToField(this.getClass());

    public static <T> LinkedHashMap<String, Field> getColumnNameToField(Class<T> clazz) {
        return getEntityFields(clazz).stream().collect(
                LinkedHashMap::new,
                (map, field) -> map.put(camelToSnake(field.getName()), field),
                LinkedHashMap::putAll
        );
    }

    public static <T> List<Field> getEntityFields(Class<T> clazz) {
        LinkedHashSet<Field> columns = new LinkedHashSet<>();
        recursiveGetEntityColumns(columns, clazz);
        return columns.stream().toList();
    }

    public static List<String> getColumnNames(BaseEntity entity) {
        List<Field> entityFields = getEntityFields(entity.getClass());
        return entityFields.stream().map(Field::getName).map(BaseEntity::camelToSnake).toList();
    }

    private static String camelToSnake(String str) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append("_").append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static List<Object> getEntityValues(BaseEntity entity) {
        LinkedHashSet<Field> columns = new LinkedHashSet<>();
        recursiveGetEntityColumns(columns, entity.getClass());
        return columns.stream().map(field -> {
            try {
                field.setAccessible(true);
                return field.get(entity);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    private static <T> void recursiveGetEntityColumns(LinkedHashSet<Field> columns, Class<T> clazz) {
        if (clazz.getSuperclass() != Object.class) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (declaredField.isAnnotationPresent(AccessIgnore.class)) {
                    continue;
                }
                columns.add(declaredField);
            }
            recursiveGetEntityColumns(columns, clazz.getSuperclass());
        }

    }

}
