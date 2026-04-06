package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record JenniferGetRealtimeDomainRequest(
    @McpToolParam(description = "Domain ID (0 for all domains, or specific domain ID like 3000)", required = false) Integer domainId
) {
}
