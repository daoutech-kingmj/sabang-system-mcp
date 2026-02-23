package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record BitbucketPrLineCommentRequest(
    @McpToolParam(description = "Bitbucket PR URL") String prUrl,
    @McpToolParam(description = "Bitbucket Personal Access Token") String pat,
    @McpToolParam(description = "Comment body (Markdown supported)") String body,
    @McpToolParam(description = "Changed file path in the PR (e.g., src/main/java/Foo.java)") String path,
    @McpToolParam(description = "Target line number in the diff") int line,
    @McpToolParam(description = "Line type in diff: ADDED, REMOVED, or CONTEXT") String lineType
) {

}
