package com.limechain.utils.json;

import lombok.extern.java.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Log
public class ObjectMapper {

    private final boolean failOnUnknownField;

    public ObjectMapper(boolean failOnUnknownField) {
        this.failOnUnknownField = failOnUnknownField;
    }

    public <T> T mapToClass(String jsonString, Class<T> clazz) {
        Map<String, Object> jsonMap = JsonUtil.parseJson(jsonString);

        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                Field field = findField(clazz, key);
                if (field != null) {
                    field.setAccessible(true);
                    field.set(instance, convertValue(field.getType(), value));
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map JSON to class", e);
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (failOnUnknownField) {
                throw new IllegalStateException("Field " + fieldName + " does not exist in " + clazz.getName());
            } else {
                log.fine("Field " + fieldName + " does not exist in " + clazz.getName());
                return null;
            }
        }
    }

    private static Object convertValue(Class<?> type, Object value) {
        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return value;
        } else if (type == Integer.class || type == int.class) {
            return ((Number) value).intValue();
        } else if (type == Double.class || type == double.class) {
            return ((Number) value).doubleValue();
        } else if (type == Boolean.class || type == boolean.class || type == byte.class) {
            return value;
        } else if (type == String.class) {
            return value.toString();
        } else if (type == byte[].class) {
            if (value instanceof String) {
                return Base64.getDecoder().decode((String) value);
            } else {
                throw new RuntimeException("Unsupported value type for byte[]: " + value.getClass());
            }
        } else if (type.isArray()) {
            return convertArray(type.getComponentType(), (List<?>) value);
        }

        // Add more type conversions as needed
        throw new RuntimeException("Unsupported field type: " + type);
    }

    private static Object convertArray(Class<?> componentType, List<?> jsonArray) {
        Object array = Array.newInstance(componentType, jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            Array.set(array, i, convertValue(componentType, jsonArray.get(i)));
        }
        return array;
    }
}