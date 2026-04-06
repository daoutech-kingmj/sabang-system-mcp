package com.mcp_server.sabang.dto;

import java.util.List;
import java.util.Map;

public record JenniferGetActiveServicesResponse(
    int domainId,
    int totalCount,
    List<Map<String, Object>> activeServices
) {
}
