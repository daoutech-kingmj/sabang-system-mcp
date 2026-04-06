package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record JenniferSearchErrorsRequest(
    @McpToolParam(description = "Domain ID (e.g. 3000 for Staging)") int domainId,
    @McpToolParam(description = "Start time in epoch milliseconds") long startTime,
    @McpToolParam(description = "End time in epoch milliseconds") long endTime,
    @McpToolParam(description = "Comma-separated instance IDs to filter (optional)", required = false) String instanceId,
    @McpToolParam(description = "Error type filter (optional, e.g. SQL_TOOMANY_FETCH, EXCEPTION)", required = false) String errorType
) {
}
