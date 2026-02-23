package com.mcp_server.sabang.exception;

/**
 * Bitbucket PR URL이 유효하지 않을 때 발생하는 예외
 */
public class InvalidPrUrlException extends RuntimeException {

    public InvalidPrUrlException(String message) {
        super(message);
    }

    public InvalidPrUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
