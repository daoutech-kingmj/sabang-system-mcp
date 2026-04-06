package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record JenniferGetTransactionsRequest(
    @McpToolParam(description = "Domain ID (e.g. 3000 for Staging)") int domainId,
    @McpToolParam(description = "Start time in epoch milliseconds") long startTime,
    @McpToolParam(description = "End time in epoch milliseconds (must be within 1 minute of start_time)") long endTime,
    @McpToolParam(description = "Instance ID to filter (optional)", required = false) Integer instanceId
) {
}
