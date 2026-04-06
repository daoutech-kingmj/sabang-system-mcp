package com.mcp_server.sabang.exception;

public class JenniferApiException extends RuntimeException {

    private final int statusCode;

    public JenniferApiException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public JenniferApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public JenniferApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
