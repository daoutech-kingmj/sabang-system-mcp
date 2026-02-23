package com.mcp_server.sabang.bitbucket;

public record BitbucketPrRef(
    String baseUrl,
    String projectKey,
    String repoSlug,
    int id
) {

}
