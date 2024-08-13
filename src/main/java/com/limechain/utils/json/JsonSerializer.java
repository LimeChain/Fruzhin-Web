package com.limechain.utils.json;

import com.limechain.utils.DivLogger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class JsonSerializer {

    private final static DivLogger LOGGER = new DivLogger();

    // Method to serialize any object to a JSON string
    public static String serializeToJson(Object object) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");

        Field[] fields = object.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                Field field = fields[i]; // To access private fields
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldValue = field.get(object);

                jsonBuilder.append("\"").append(fieldName).append("\":");
                appendValue(jsonBuilder, fieldValue);

                if (i < fields.length - 1) {
                    jsonBuilder.append(",");
                }
            } catch (IllegalAccessException e) {
                LOGGER.log(Level.FINE, Arrays.toString(e.getStackTrace()));
            }
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    // Helper method to handle different types of values
    private static void appendValue(StringBuilder jsonBuilder, Object value) {
        if (value == null) {
            jsonBuilder.append("null");
        } else if (value instanceof String) {
            jsonBuilder.append("\"").append(value).append("\"");
        } else if (value instanceof Number || value instanceof Boolean) {
            jsonBuilder.append(value);
        } else if (value instanceof List) {
            appendList(jsonBuilder, (List<?>) value);
        } else if (value instanceof Map) {
            appendMap(jsonBuilder, (Map<?, ?>) value);
        } else if (value instanceof byte[]) {
            appendByteArray(jsonBuilder, (byte[]) value);
        } else if (value.getClass().isArray()) {
            appendArray(jsonBuilder, value);
        } else {
            jsonBuilder.append(serializeToJson(value)); // Recursively handle nested objects
        }
    }

    // Method to serialize a List to JSON
    private static void appendList(StringBuilder jsonBuilder, List<?> list) {
        jsonBuilder.append("[");
        for (int i = 0; i < list.size(); i++) {
            appendValue(jsonBuilder, list.get(i));
            if (i < list.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
    }

    // Method to serialize a Map to JSON
    private static void appendMap(StringBuilder jsonBuilder, Map<?, ?> map) {
        jsonBuilder.append("{");
        Set<?> keys = map.keySet();
        int i = 0;
        for (Object key : keys) {
            jsonBuilder.append("\"").append(key).append("\":");
            appendValue(jsonBuilder, map.get(key));
            if (i < keys.size() - 1) {
                jsonBuilder.append(",");
            }
            i++;
        }
        jsonBuilder.append("}");
    }

    // Method to serialize an array to JSON
    private static void appendArray(StringBuilder jsonBuilder, Object array) {
        jsonBuilder.append("[");
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            appendValue(jsonBuilder, Array.get(array, i));
            if (i < length - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
    }

    private static void appendByteArray(StringBuilder jsonBuilder, byte[] byteArray) {
        String base64 = Base64.getEncoder().encodeToString(byteArray);
        jsonBuilder.append("\"").append(base64).append("\"");
    }
}