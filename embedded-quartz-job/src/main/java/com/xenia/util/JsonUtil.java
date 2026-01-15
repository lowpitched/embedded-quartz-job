package com.xenia.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) {
        try {
            if (object == null) {
                return null;
            }
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            if (json == null || json.isEmpty()) {
                return null;
            }
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
