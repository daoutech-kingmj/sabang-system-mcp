package com.mcp_server.sabang.dto;

public record BitbucketPrVersion(
    String id,
    String displayId,
    long authorTimestamp,
    String message
) {

}
