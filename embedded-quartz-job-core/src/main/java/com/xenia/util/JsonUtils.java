package com.xenia.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtils {

    public final static ObjectMapper OBJECTMAPPER = new ObjectMapper();

    static {
        OBJECTMAPPER.registerModule(new JavaTimeModule());
    }

    public static String toJson(Object object) {
        try {
            if (object == null) {
                return null;
            }
            return OBJECTMAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            if (json == null || json.isEmpty()) {
                return null;
            }
            return OBJECTMAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
