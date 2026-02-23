package com.mcp_server.sabang.dto;

public record GitlabMrCommentResponse(
    String mrUrl,
    long noteId,
    String webUrl
) {

}

