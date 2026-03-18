package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record PinpointGetResponseHistogramRequest(
    @McpToolParam(description = "Application name registered in Pinpoint") String applicationName,
    @McpToolParam(description = "Service type name (e.g. SPRING_BOOT, TOMCAT, STAND_ALONE)") String serviceTypeName,
    @McpToolParam(description = "Start time in epoch milliseconds") long from,
    @McpToolParam(description = "End time in epoch milliseconds") long to
) {
}
