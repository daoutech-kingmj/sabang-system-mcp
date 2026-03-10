package com.mcp_server.sabang.dto;

import java.util.List;
import org.springaicommunity.mcp.annotation.McpToolParam;

public record SparrowAnalyzeRequest(
    @McpToolParam(description = "SPARROW Server URL (e.g., https://123.2.134.11:18080)") String serverUrl,
    @McpToolParam(description = "Path to SPARROW client shell script") String clientPath,
    @McpToolParam(description = "Path to password file") String passwordPath,
    @McpToolParam(description = "SPARROW Project ID") String projectId,
    @McpToolParam(description = "SPARROW username") String username,
    @McpToolParam(description = "List of changed Java files, each specified as an absolute filesystem path")
    List<String> changedFiles
) {

}
