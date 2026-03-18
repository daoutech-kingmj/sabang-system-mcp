package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record PinpointGetErrorTransactionsRequest(
    @McpToolParam(description = "Application name (e.g. Dev-Customer, Prod-Extra)") String applicationName,
    @McpToolParam(description = "Start time in epoch milliseconds") long from,
    @McpToolParam(description = "End time in epoch milliseconds") long to,
    @McpToolParam(description = "Optional agent ID to filter by specific agent", required = false) String agentId,
    @McpToolParam(description = "Max number of transactions to return (default 50)", required = false) Integer limit
) {
}
