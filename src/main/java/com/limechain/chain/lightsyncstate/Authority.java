package com.limechain.chain.lightsyncstate;

import com.limechain.teavm.annotation.Reflectable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigInteger;

@Getter
@Setter
@Reflectable
@AllArgsConstructor
@NoArgsConstructor
public class Authority implements Serializable {
    private byte[] publicKey;
    private BigInteger weight;
}
