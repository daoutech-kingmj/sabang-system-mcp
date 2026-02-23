package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record GitlabMrCommentRequest(
    @McpToolParam(description = "GitLab MR URL") String mrUrl,
    @McpToolParam(description = "GitLab Personal Access Token") String pat,
    @McpToolParam(description = "Comment body (Markdown supported)") String body
) {

}

