package com.mcp_server.sabang.exception;

public class OpenSearchApiException extends RuntimeException {

    private final int statusCode;

    public OpenSearchApiException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public OpenSearchApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public OpenSearchApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}