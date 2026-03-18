package com.mcp_server.sabang.dto;

import java.util.Map;

public record PinpointGetDetailedAgentInfoResponse(
    String agentId,
    Map<String, Object> agentInfo
) {
}
