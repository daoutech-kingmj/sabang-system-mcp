package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record PinpointGetAgentsRequest(
    @McpToolParam(description = "Application name (e.g. Dev-Customer, Stage-Extra, Prod-user-api)") String applicationName
) {
}
