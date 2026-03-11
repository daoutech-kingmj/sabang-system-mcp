package com.mcp_server.sabang.exception;

public class GrafanaApiException extends RuntimeException {

    private final int statusCode;

    public GrafanaApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public GrafanaApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}