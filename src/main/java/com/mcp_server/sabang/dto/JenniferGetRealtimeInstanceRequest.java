package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record JenniferGetRealtimeInstanceRequest(
    @McpToolParam(description = "Domain ID (e.g. 3000 for Staging)") int domainId,
    @McpToolParam(description = "Instance ID (0 for all instances in the domain, or specific instance ID)", required = false) Integer instanceId
) {
}
