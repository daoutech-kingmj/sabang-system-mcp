package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record GitlabMrLineCommentRequest(
    @McpToolParam(description = "GitLab MR URL") String mrUrl,
    @McpToolParam(description = "GitLab Personal Access Token") String pat,
    @McpToolParam(description = "Comment body (Markdown supported)") String body,
    @McpToolParam(description = "Changed file path in the MR (e.g., src/main/java/Foo.java)") String path,
    @McpToolParam(description = "Target line number in the diff") int line,
    @McpToolParam(description = "Line type in diff: ADDED or REMOVED") String lineType
) {

}
