package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record JenniferGetInstancesRequest(
    @McpToolParam(description = "Domain ID (e.g. 3000 for Staging, 1001 for DEV, 2000 for CUWS_G1)") int domainId
) {
}
