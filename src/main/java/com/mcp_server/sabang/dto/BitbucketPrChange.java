package com.mcp_server.sabang.dto;

public record BitbucketPrChange(
    String path,
    String type,
    String nodeType,
    boolean executable
) {

}
