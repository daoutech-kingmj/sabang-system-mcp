package com.mcp_server.sabang.tool;

import com.mcp_server.sabang.dto.GitlabMrCommentRequest;
import com.mcp_server.sabang.dto.GitlabMrCommentResponse;
import com.mcp_server.sabang.dto.GitlabMrGetRequest;
import com.mcp_server.sabang.dto.GitlabMrGetResponse;
import com.mcp_server.sabang.dto.GitlabMrLineCommentRequest;
import com.mcp_server.sabang.service.GitlabMrService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class GitlabMrTools {
    private final GitlabMrService gitlabMrService;

    public GitlabMrTools(GitlabMrService gitlabMrService) {
        this.gitlabMrService = gitlabMrService;
    }

    @McpTool(name="gitlab-mr-get", description="Get GitLab merge request details and changes", generateOutputSchema=true)
    public GitlabMrGetResponse getMergeRequest(GitlabMrGetRequest request) {
        return this.gitlabMrService.getMergeRequest(request);
    }

    @McpTool(name="gitlab-mr-comment-post", description="Post a general comment to a GitLab merge request", generateOutputSchema=true)
    public GitlabMrCommentResponse postComment(GitlabMrCommentRequest request) {
        return this.gitlabMrService.postComment(request);
    }

    @McpTool(name="gitlab-mr-line-comment-post", description="Post a line comment to a GitLab merge request", generateOutputSchema=true)
    public GitlabMrCommentResponse postLineComment(GitlabMrLineCommentRequest request) {
        return this.gitlabMrService.postLineComment(request);
    }
}
