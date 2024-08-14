package com.limechain.utils.json;

import com.limechain.utils.DivLogger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ObjectMapper {

    private static final DivLogger LOGGER = new DivLogger();

    private final boolean failOnUnknownField;

    public ObjectMapper(boolean failOnUnknownField) {
        this.failOnUnknownField = failOnUnknownField;
    }

    @SuppressWarnings("unchecked")
    public <T> T mapToClass(String jsonString, Class<T> clazz) {
        Object parsed = JsonUtil.parseJson(jsonString);

        if (isPrimitiveOrWrapper(clazz) || clazz == String.class || clazz.isArray() || clazz == byte[].class) {
            return convertValue(clazz, parsed);
        }

        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) parsed).entrySet()) {
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
                LOGGER.log(Level.FINE, "Field " + fieldName + " does not exist in " + clazz.getName());
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convertValue(Class<T> type, Object value) {
        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return (T) value;
        } else if (type == Integer.class || type == int.class) {
            return (T) (Integer) ((Number) value).intValue();
        } else if (type == Double.class || type == double.class) {
            return (T) (Double) ((Number) value).doubleValue();
        } else if (type == Boolean.class || type == boolean.class) {
            return (T) value;
        } else if (type == String.class) {
            return (T) value.toString();
        } else if (type == byte[].class) {
            if (value instanceof String) {
                return (T) Base64.getDecoder().decode((String) value);
            } else {
                throw new RuntimeException("Unsupported value type for byte[]: " + value.getClass());
            }
        } else if (type.isArray()) {
            return (T) convertArray(type.getComponentType(), (List<?>) value);
        }

        throw new RuntimeException("Unsupported field type: " + type);
    }

    private Object convertArray(Class<?> componentType, List<?> jsonArray) {
        Object array = Array.newInstance(componentType, jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            Array.set(array, i, convertValue(componentType, jsonArray.get(i)));
        }
        return array;
    }

    // Utility method to check if a class is a primitive type or its wrapper
    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
            clazz == Integer.class || clazz == Long.class ||
            clazz == Double.class || clazz == Float.class ||
            clazz == Boolean.class || clazz == Byte.class ||
            clazz == Short.class || clazz == Character.class;
    }
}