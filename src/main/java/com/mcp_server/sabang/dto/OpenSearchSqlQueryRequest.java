package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record OpenSearchSqlQueryRequest(
    @McpToolParam(description = "SQL query to execute (e.g. SELECT * FROM my-index WHERE field = 'value' LIMIT 10)") String query
) {

}