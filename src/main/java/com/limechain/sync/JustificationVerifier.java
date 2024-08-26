package com.limechain.sync;

import com.limechain.chain.lightsyncstate.Authority;
import com.limechain.network.protocol.warp.dto.Precommit;
import com.limechain.polkaj.Hash256;
import com.limechain.polkaj.Hash512;
import com.limechain.rpc.server.AppBean;
import com.limechain.storage.block.SyncState;
import com.limechain.utils.LittleEndianUtils;
import com.limechain.utils.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.teavm.jso.JSBody;
import org.teavm.jso.core.JSBoolean;
import org.teavm.jso.core.JSPromise;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JustificationVerifier {
    public static boolean verify(Precommit[] precommits, BigInteger round) {
        SyncState syncState = AppBean.getBean(SyncState.class);
        Authority[] authorities = syncState.getAuthoritySet();
        BigInteger authoritiesSetId = syncState.getSetId();

        // Implementation from: https://github.com/smol-dot/smoldot
        // lib/src/finality/justification/verify.rs
        if (authorities == null || precommits.length < (authorities.length * 2 / 3) + 1) {
            log.log(Level.WARNING, "Not enough signatures");
            return false;
        }

        Set<Hash256> seenPublicKeys = new HashSet<>();
        Set<Hash256> authorityKeys =
                Arrays.stream(authorities).map(Authority::getPublicKey).map(Hash256::new).collect(Collectors.toSet());

        for (Precommit precommit : precommits) {
            if (!authorityKeys.contains(precommit.getAuthorityPublicKey())) {
                log.log(Level.WARNING, "Invalid Authority for precommit");
                return false;
            }

            if (seenPublicKeys.contains(precommit.getAuthorityPublicKey())) {
                log.log(Level.WARNING, "Duplicated signature");
                return false;
            }
            seenPublicKeys.add(precommit.getAuthorityPublicKey());

            // TODO (from smoldot): must check signed block ancestry using `votes_ancestries`

            byte[] data = getDataToVerify(precommit, authoritiesSetId, round);

            boolean isValid =
                    verifySignature(precommit.getAuthorityPublicKey().toString(), precommit.getSignature().toString(),
                            data);
            if (!isValid) {
                log.log(Level.WARNING, "Failed to verify signature");
                return false;
            }
        }
        log.log(Level.INFO, "All signatures were verified successfully");

        // From Smoldot implementation:
        // TODO: must check that votes_ancestries doesn't contain any unused entry
        // TODO: there's also a "ghost" thing?

        return true;
    }

    private static byte[] getDataToVerify(Precommit precommit, BigInteger authoritiesSetId, BigInteger round) {
        // 1 reserved byte for data type
        // 32 reserved for target hash
        // 4 reserved for block number
        // 8 reserved for justification round
        // 8 reserved for set id
        int messageCapacity = 1 + 32 + 4 + 8 + 8;
        var messageBuffer = ByteBuffer.allocate(messageCapacity);
        messageBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Write message type
        messageBuffer.put((byte) 1);
        // Write target hash
        messageBuffer.put(LittleEndianUtils.convertBytes(StringUtils.hexToBytes(precommit.getTargetHash().toString())));
        //Write Justification round bytes as u64
        messageBuffer.put(LittleEndianUtils.bytesToFixedLength(precommit.getTargetNumber().toByteArray(), 4));
        //Write Justification round bytes as u64
        messageBuffer.put(LittleEndianUtils.bytesToFixedLength(round.toByteArray(), 8));
        //Write Set Id bytes as u64
        messageBuffer.put(LittleEndianUtils.bytesToFixedLength(authoritiesSetId.toByteArray(), 8));

        //Verify message
        //Might have problems because we use the stand ED25519 instead of ED25519_zebra
        messageBuffer.rewind();
        byte[] data = new byte[messageBuffer.remaining()];
        messageBuffer.get(data);
        return data;
    }

    public static boolean verifySignature(String publicKeyHex, String signatureHex, byte[] data) {
        String message = StringUtils.toHex(data);
        AtomicBoolean verifier = new AtomicBoolean(false);
        Object lock = new Object();

        verifySignature(publicKeyHex, signatureHex, message).then(isValid -> {
            synchronized (lock) {
                verifier.set(isValid.booleanValue());
                lock.notify();
            }
            return null;
        });

        synchronized (lock) {
            try {
                lock.wait();

                boolean result = verifier.get();
                if (!result) {
                    log.log(Level.WARNING, "Invalid signature");
                }
                return result;
            } catch (InterruptedException e) {
                log.log(Level.WARNING, "Interrupted while waiting for signature verification");
                return false;
            }
        }
    }

    @JSBody(params = {"publicKeyHex", "signatureHex", "messageHex"},
            script = "return (async () => {" +
                     "  const publicKeyBytes = new Uint8Array([...publicKeyHex.matchAll(/../g)].map(m => parseInt(m[0], 16)));" +
                     "  const signatureBytes = new Uint8Array([...signatureHex.matchAll(/../g)].map(m => parseInt(m[0], 16)));" +
                     "  const publicKey = await crypto.subtle.importKey(" +
                     "    'raw'," + "    publicKeyBytes," +
                     "    {" + "      name: 'NODE-ED25519'," +
                     "      namedCurve: 'ed25519'" + "    }," +
                     "    true," + "    ['verify']" + "  );" +
                     "  const messageBytes = new Uint8Array([...messageHex.matchAll(/../g)].map(m => parseInt(m[0], 16)));;" +
                     "  const isValid = await crypto.subtle.verify(" +
                     "    {" + "      name: 'NODE-ED25519'" +
                     "    }," + "    publicKey," +
                     "    signatureBytes," +
                     "    messageBytes" + "  );" +
                     "  return isValid;" +

                     "})()")
    public static native JSPromise<JSBoolean> verifySignature(String publicKeyHex, String signatureHex,
                                                              String messageHex);
}
