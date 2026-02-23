package com.mcp_server.sabang.dto;

public record SparrowAnalyzeResponse(
    String projectId,
    int exitCode,
    String output,
    String error
) {

}
