package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record BitbucketPrCommentRequest(
    @McpToolParam(description = "Bitbucket PR URL") String prUrl,
    @McpToolParam(description = "Bitbucket Personal Access Token") String pat,
    @McpToolParam(description = "Comment body (Markdown supported)") String body
) {

}
