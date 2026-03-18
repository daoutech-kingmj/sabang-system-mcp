package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record PinpointGetTransactionsRequest(
    @McpToolParam(description = "Application name (e.g. Dev-Customer, Prod-Extra)") String applicationName,
    @McpToolParam(description = "Start time in epoch milliseconds") long from,
    @McpToolParam(description = "End time in epoch milliseconds") long to,
    @McpToolParam(description = "Optional agent ID to filter by specific agent", required = false) String agentId,
    @McpToolParam(description = "Max number of transactions to return (default 50)", required = false) Integer limit,
    @McpToolParam(description = "Minimum response time in ms to filter slow transactions (default 0). Use 3000 to find slow/error transactions.", required = false) Integer minResponseTime,
    @McpToolParam(description = "Maximum response time in ms (default 600000)", required = false) Integer maxResponseTime
) {
}