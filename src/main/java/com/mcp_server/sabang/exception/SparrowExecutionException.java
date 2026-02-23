package com.mcp_server.sabang.exception;

public class SparrowExecutionException extends RuntimeException {

    public SparrowExecutionException(String message) {
        super(message);
    }

    public SparrowExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
