package com.mcp_server.sabang.dto;

import java.util.Map;

public record PinpointGetServerMapResponse(
    String applicationName,
    Map<String, Object> serverMapData
) {
}
