package com.mcp_server.sabang.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitlabMrVersion(
    @JsonProperty(value = "base_commit_sha") String baseCommitSha,
    @JsonProperty(value = "head_commit_sha") String headCommitSha,
    @JsonProperty(value = "start_commit_sha") String startCommitSha
) {

}

