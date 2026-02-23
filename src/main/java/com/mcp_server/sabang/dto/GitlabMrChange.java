package com.mcp_server.sabang.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitlabMrChange(
    @JsonProperty(value = "old_path") String oldPath,
    @JsonProperty(value = "new_path") String newPath,
    @JsonProperty(value = "new_file") boolean newFile,
    @JsonProperty(value = "renamed_file") boolean renamedFile,
    @JsonProperty(value = "deleted_file") boolean deletedFile,
    @JsonProperty(value = "diff") String diff
) {

}

