package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record JenniferGetActiveServicesRequest(
    @McpToolParam(description = "Domain ID (e.g. 3000 for Staging)") int domainId,
    @McpToolParam(description = "Comma-separated instance IDs to filter (optional)", required = false) String instanceId
) {
}
