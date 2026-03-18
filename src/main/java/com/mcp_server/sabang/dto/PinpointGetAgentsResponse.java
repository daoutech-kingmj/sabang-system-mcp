package com.mcp_server.sabang.dto;

import java.util.List;

public record PinpointGetAgentsResponse(
    String applicationName,
    List<AgentInfo> agents
) {

    public record AgentInfo(
        String agentId,
        String hostName,
        String ip,
        String serviceType
    ) {
    }
}
