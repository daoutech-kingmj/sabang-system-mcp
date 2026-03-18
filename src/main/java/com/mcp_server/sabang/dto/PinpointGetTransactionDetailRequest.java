package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record PinpointGetTransactionDetailRequest(
    @McpToolParam(description = "Trace ID of the transaction to look up") String traceId,
    @McpToolParam(description = "Focus timestamp (use 0 for default)", required = false) Long focusTimestamp
) {
}
