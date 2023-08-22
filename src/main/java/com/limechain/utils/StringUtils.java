package com.limechain.utils;

import com.google.protobuf.ByteString;
import lombok.experimental.UtilityClass;

import java.security.InvalidParameterException;
import java.util.regex.Pattern;

@UtilityClass
public class StringUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("^(0x)?[0-9a-fA-F]+$");

    public static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new InvalidParameterException("Invalid hex string length");
        }

        if (!HEX_PATTERN.matcher(hex).matches()) {
            throw new InvalidParameterException("Invalid hex string");
        }

        // Trim the 0x prefix if it exists
        if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }

        return ByteString.fromHex(hex).toByteArray();
    }

    public static String remove0xPrefix(String hex) {
        if (hex.startsWith("0x")) {
            return hex.substring(2);
        }
        return hex;
    }

    public static String toHex(String key) {
        StringBuilder sb = new StringBuilder();
        char[] ch = key.toCharArray();
        for (char c : ch) {
            String hexString = Integer.toHexString(c);
            sb.append(hexString);
        }
        return sb.toString();
    }
}