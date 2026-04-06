package com.mcp_server.sabang.dto;

import java.util.Map;

public record JenniferGetInstanceMetricsResponse(
    int domainId,
    int instanceId,
    Map<String, Object> metricsData
) {
}
