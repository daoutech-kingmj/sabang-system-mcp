package com.mcp_server.sabang.exception;

/**
 * GitLab MR URL이 유효하지 않을 때 발생하는 예외
 */
public class InvalidMrUrlException extends RuntimeException {

    public InvalidMrUrlException(String message) {
        super(message);
    }

    public InvalidMrUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
