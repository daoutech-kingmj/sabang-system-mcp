package com.mcp_server.sabang.exception;

/**
 * Bitbucket API 호출 중 발생하는 예외를 처리하기 위한 커스텀 예외
 */
public class BitbucketApiException extends RuntimeException {

    private final int statusCode;

    public BitbucketApiException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public BitbucketApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public BitbucketApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
