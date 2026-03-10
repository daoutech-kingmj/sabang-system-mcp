package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record OpenSearchListIndicesRequest(
    @McpToolParam(description = "Optional index name pattern to filter (e.g. work-notification*). Leave empty for all indices.") String pattern
) {

}