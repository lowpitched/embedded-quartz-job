package com.xenia.core.po;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;

@Getter
@Setter
public class BaseEntity {

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    @AccessIgnore
    private List< String> columnNames = getColumnNames(this.getClass()) ;

    public static <T> List<String> getColumnNames(Class<T> clazz) {
        LinkedHashSet<Field> columns = new LinkedHashSet<>();
        recursiveGetEntityColumns(columns, clazz);
        return columns.stream().map(Field::getName).toList();
    }

    public static List<Object> getColumnValues(BaseEntity entity) {
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
