package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record PinpointGetAgentStatRequest(
    @McpToolParam(description = "Agent ID to query stats for") String agentId,
    @McpToolParam(description = "Chart type: jvmGc, cpuLoad, activeTrace, transaction, responseTime, dataSource, totalThreadCount, loadedClass, fileDescriptor, directBuffer") String chartType,
    @McpToolParam(description = "Start time in epoch milliseconds") long from,
    @McpToolParam(description = "End time in epoch milliseconds") long to
) {
}
