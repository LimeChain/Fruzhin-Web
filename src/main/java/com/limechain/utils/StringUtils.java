package com.limechain.utils;

import lombok.experimental.UtilityClass;
import org.teavm.jso.JSBody;

@UtilityClass
public class StringUtils {
    public static final String HEX_PREFIX = "0x";

    /**
     * Converts a hexadecimal string into an array of bytes.
     * The method first checks if the hex string has an even length and if it matches the hex pattern.
     * It then removes the "0x" prefix if present and converts the cleaned hex string to a byte array.
     *
     * @param hex the hexadecimal string to convert
     * @return the corresponding byte array
     * @throws IllegalArgumentException if the hex string has an odd length or does not match the hex pattern
     */
    public static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string length");
        }

        // Trim the 0x prefix if it exists
        hex = remove0xPrefix(hex);

        return fromHex(hex);
    }

    public byte[] fromHex(String hex){
        return fromHexNative(hex.startsWith("0x") ? hex.substring(2) : hex);
    }

    @JSBody(params = {"hex"}, script = "let bytes = [];" +
                                       "    for (let c = 0; c < hex.length; c += 2)" +
                                       "        bytes.push(parseInt(hex.substr(c, 2), 16));" +
                                       "    return bytes;")
    private static native byte[] fromHexNative(String hex);

    /**
     * Removes the "0x" prefix from a hexadecimal string, if it exists.
     * This is useful for cleaning up hex strings to ensure they can be processed or parsed elsewhere.
     *
     * @param hex the hexadecimal string from which the prefix should be removed
     * @return the hex string without the "0x" prefix
     */
    public static String remove0xPrefix(String hex) {
        if (hex.startsWith(HEX_PREFIX)) {
            return hex.substring(2);
        }
        return hex;
    }

    private final String hexArray = "0123456789abcdef";

    public static String toHex(byte[] bytes) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i != bytes.length; i++) {
            int v = bytes[i] & 0xff;
            buf.append(hexArray.charAt(v >> 4));
            buf.append(hexArray.charAt(v & 0xf));
        }
        return buf.toString();
    }

    /**
     * Converts a byte array to a hexadecimal string with a "0x" prefix.
     * This is commonly used to represent binary data in a readable hex format prefixed to indicate its base.
     *
     * @param bytes the byte array to convert
     * @return the "0x" prefixed hexadecimal string
     */
    public static String toHexWithPrefix(byte[] bytes) {
        return HEX_PREFIX + toHex(bytes);
    }
}
