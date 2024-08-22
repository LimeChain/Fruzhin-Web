package com.limechain.network.protocol.warp.scale.reader;

import com.limechain.network.protocol.warp.dto.BlockHeader;
import com.limechain.network.protocol.warp.dto.HeaderDigest;
import com.limechain.polkaj.Hash256;
import com.limechain.polkaj.reader.ScaleCodecReader;
import com.limechain.polkaj.reader.ScaleReader;

import java.math.BigInteger;

public class BlockHeaderReader implements ScaleReader<BlockHeader> {
    @Override
    public BlockHeader read(ScaleCodecReader reader) {
        BlockHeader blockHeader = new BlockHeader();
        System.out.println("BlockHeaderReader.read");
        blockHeader.setParentHash(new Hash256(reader.readUint256()));
        System.out.println("BlockHeaderReader.setParentHash");
        // NOTE: Usage of BlockNumberReader is intentionally omitted here,
        //  since we want this to be a compact int, not a var size int
        blockHeader.setBlockNumber(BigInteger.valueOf(reader.readCompactInt()));
        System.out.println("BlockHeaderReader.setBlockNumber");
        blockHeader.setStateRoot(new Hash256(reader.readUint256()));
        System.out.println("BlockHeaderReader.setStateRoot");
        blockHeader.setExtrinsicsRoot(new Hash256(reader.readUint256()));
        System.out.println("BlockHeaderReader.setExtrinsicsRoot");

        var digestCount = reader.readCompactInt();
        HeaderDigest[] digests = new HeaderDigest[digestCount];
        for (int i = 0; i < digestCount; i++) {
            digests[i] = new HeaderDigestReader().read(reader);
        }

        blockHeader.setDigest(digests);
        System.out.println("BlockHeaderReader.setDigest");

        return blockHeader;
    }
}
