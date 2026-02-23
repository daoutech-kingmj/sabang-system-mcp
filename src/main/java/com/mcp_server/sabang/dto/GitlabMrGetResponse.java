package com.mcp_server.sabang.dto;

import java.util.List;

public record GitlabMrGetResponse(
    String mrUrl,
    String baseUrl,
    String projectPath,
    String projectIdEnc,
    int iid,
    List<GitlabMrChange> changes,
    List<GitlabMrVersion> versions
) {

}

