package com.limechain.utils;

import io.emeraldpay.polkaj.types.Hash256;
import org.bouncycastle.crypto.digests.Blake2bDigest;

public class HashUtils {
    public static final int HASH256_HASH_LENGTH = Hash256.SIZE_BYTES * Byte.SIZE;
    public static byte[] hashWithBlake2b(byte[] input) {
        Blake2bDigest digest = new Blake2bDigest(HASH256_HASH_LENGTH);
        digest.reset();
        digest.update(input, 0, input.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return hash;
    }

    public static byte[] hashWithBlake2bToLength(byte[] input, int length) {
        Blake2bDigest digest = new Blake2bDigest(length * Byte.SIZE);
        digest.reset();
        digest.update(input, 0, input.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return hash;
    }
}
