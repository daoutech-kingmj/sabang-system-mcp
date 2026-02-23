package com.mcp_server.sabang.dto;

public record GitlabMrRef(
    String baseUrl,
    String projectPath,
    String projectIdEnc,
    int iid
) {

}

