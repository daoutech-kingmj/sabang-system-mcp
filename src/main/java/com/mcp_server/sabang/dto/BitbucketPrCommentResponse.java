package com.mcp_server.sabang.dto;

public record BitbucketPrCommentResponse(
    String prUrl,
    long commentId,
    String webUrl
) {

}
