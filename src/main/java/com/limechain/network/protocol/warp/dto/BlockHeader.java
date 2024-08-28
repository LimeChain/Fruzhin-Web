package com.limechain.network.protocol.warp.dto;

import com.limechain.network.protocol.blockannounce.scale.BlockHeaderScaleWriter;
import com.limechain.polkaj.Hash256;
import com.limechain.utils.HashUtils;
import com.limechain.utils.StringUtils;
import com.limechain.utils.scale.ScaleUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.teavm.jso.core.JSString;

import java.math.BigInteger;
import java.util.Arrays;

@Setter
@Getter
@ToString
public class BlockHeader {
    // TODO: Make this const configurable
    public static final int BLOCK_NUMBER_SIZE = 4;


    private Hash256 parentHash;
    private BigInteger blockNumber;
    private Hash256 stateRoot;
    private Hash256 extrinsicsRoot;
    private HeaderDigest[] digest;

    @Override
    public String toString() {
        return "BlockHeader{" +
               "parentHash=" + parentHash +
               ", blockNumber=" + blockNumber +
               ", stateRoot=" + stateRoot +
               ", extrinsicsRoot=" + extrinsicsRoot +
               ", digest=" + Arrays.toString(digest) +
               '}';
    }

    public Hash256 getHash() {
        byte[] scaleEncoded = ScaleUtils.Encode.encode(BlockHeaderScaleWriter.getInstance(), this);
        JSString jsString = HashUtils.hashWithBlake2b(StringUtils.toHex(scaleEncoded));
        return new Hash256(StringUtils.hexToBytes(jsString.stringValue()));
    }
}
