package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record SparrowAnalyzeJobStatusRequest(
        @McpToolParam(description = "SPARROW analysis job ID") String jobId,
        @McpToolParam(description = "Long-poll wait time in seconds (1-30 recommended)") int waitSeconds
) {

}
