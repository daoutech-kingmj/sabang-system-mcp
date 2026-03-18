package com.mcp_server.sabang.dto;

import java.util.Map;

public record PinpointGetResponseHistogramResponse(
    String applicationName,
    Map<String, Object> histogramData
) {
}
