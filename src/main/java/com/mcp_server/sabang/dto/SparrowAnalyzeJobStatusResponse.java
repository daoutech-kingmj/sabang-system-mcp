package com.mcp_server.sabang.dto;

import java.time.Instant;

public record SparrowAnalyzeJobStatusResponse(
        String jobId,
        String projectId,
        String status,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt,
        Integer exitCode,
        String output,
        String error,
        String message
) {

}
