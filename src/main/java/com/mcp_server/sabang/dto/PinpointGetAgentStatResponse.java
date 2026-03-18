package com.mcp_server.sabang.dto;

import java.util.Map;

public record PinpointGetAgentStatResponse(
    String agentId,
    String chartType,
    Map<String, Object> chartData
) {
}
