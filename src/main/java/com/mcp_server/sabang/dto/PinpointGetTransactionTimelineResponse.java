package com.mcp_server.sabang.dto;

import java.util.Map;

public record PinpointGetTransactionTimelineResponse(
    String traceId,
    Map<String, Object> timelineData
) {
}
