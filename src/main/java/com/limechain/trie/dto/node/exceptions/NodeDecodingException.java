package com.limechain.trie.dto.node.exceptions;

public class NodeDecodingException extends RuntimeException {
    public NodeDecodingException(Throwable cause) {
        super(cause);
    }

    public NodeDecodingException(String message) {
        super(message);
    }

    public NodeDecodingException(String message, Throwable cause) {
        super(message, cause);
    }
}