package com.limechain.utils;

import com.limechain.network.protocol.warp.dto.BlockHeader;
import com.limechain.network.protocol.warp.dto.HeaderDigest;
import com.limechain.network.protocol.warp.scale.reader.HeaderDigestReader;
import com.limechain.polkaj.Hash256;
import com.limechain.polkaj.reader.ScaleCodecReader;
import com.limechain.rpc.dto.ChainGetHeaderResult;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RpcUtils {

    public static BlockHeader toBlockHeader(ChainGetHeaderResult result) {
        BlockHeader header = new BlockHeader();

        header.setBlockNumber(new BigInteger(
                StringUtils.remove0xPrefix(result.getNumber()), 16));
        header.setParentHash(Hash256.from(result.getParentHash()));
        header.setStateRoot(Hash256.from(result.getStateRoot()));
        header.setExtrinsicsRoot(Hash256.from(result.getExtrinsicsRoot()));

        List<String> digestHexes = result.getDigest().getLogs();
        HeaderDigest[] digests = new HeaderDigest[digestHexes.size()];
        for (int i = 0; i < digestHexes.size(); i++) {
            digests[i] = new HeaderDigestReader().read(
                    new ScaleCodecReader(StringUtils.hexToBytes(digestHexes.get(i))));
        }
        header.setDigest(digests);

        return header;
    }
}
