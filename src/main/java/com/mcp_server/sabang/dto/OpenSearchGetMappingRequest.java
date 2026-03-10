package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record OpenSearchGetMappingRequest(
    @McpToolParam(description = "Index name to get mapping for (e.g. work-notification-event-202603)") String index
) {

}