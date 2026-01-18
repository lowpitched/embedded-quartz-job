package com.xenia.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity {

    @AccessIgnore
    private Long id;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    /*private String createdBy;

    private String updatedBy;*/

    @AccessIgnore
    @JsonIgnore
    private List<Field> columnNames = getEntityFields(this.getClass());

    @AccessIgnore
    @JsonIgnore
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
        if (clazz == Object.class) {
            return;
        }
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
