package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record PinpointGetDetailedAgentInfoRequest(
    @McpToolParam(description = "Agent ID to query detailed info for") String agentId,
    @McpToolParam(description = "Timestamp in epoch milliseconds (use current time)") long timestamp
) {
}
