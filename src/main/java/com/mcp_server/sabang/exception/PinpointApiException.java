package com.mcp_server.sabang.exception;

public class PinpointApiException extends RuntimeException {

    private final int statusCode;

    public PinpointApiException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public PinpointApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public PinpointApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
