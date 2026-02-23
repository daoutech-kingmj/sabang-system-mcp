package com.mcp_server.sabang.dto;

import java.util.List;

public record BitbucketPrGetResponse(
    String prUrl,
    String baseUrl,
    String projectKey,
    String repoSlug,
    int id,
    List<BitbucketPrChange> changes,
    List<BitbucketPrVersion> versions
) {

}
