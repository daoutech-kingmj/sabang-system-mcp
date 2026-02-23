package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record BitbucketPrGetRequest(
    @McpToolParam(description = "Bitbucket PR URL") String prUrl,
    @McpToolParam(description = "Bitbucket Personal Access Token") String pat
) {

}
