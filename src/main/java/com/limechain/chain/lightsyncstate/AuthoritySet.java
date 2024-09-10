package com.limechain.chain.lightsyncstate;

import com.limechain.polkaj.Hash256;
import com.limechain.tuple.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

@Getter
@Setter
@ToString
public class AuthoritySet {
    private Authority[] currentAuthorities;
    private BigInteger setId;
    private ForkTree<PendingChange> pendingStandardChanges;
    private PendingChange[] pendingForcedChanges;
    private Pair<BigInteger, Hash256>[] authoritySetChanges;
}
