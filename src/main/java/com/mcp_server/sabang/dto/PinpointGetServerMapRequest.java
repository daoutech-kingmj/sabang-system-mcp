package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record PinpointGetServerMapRequest(
    @McpToolParam(description = "Application name (e.g. Dev-Customer, Prod-Extra)") String applicationName,
    @McpToolParam(description = "Service type code (e.g. 1010 for TOMCAT, 1210 for SPRING_BOOT, 1000 for STAND_ALONE)") int serviceTypeCode,
    @McpToolParam(description = "Start time in epoch milliseconds") long from,
    @McpToolParam(description = "End time in epoch milliseconds") long to
) {
}
