package com.mcp_server.sabang.tool;

import com.mcp_server.sabang.dto.BitbucketPrCommentRequest;
import com.mcp_server.sabang.dto.BitbucketPrCommentResponse;
import com.mcp_server.sabang.dto.BitbucketPrGetRequest;
import com.mcp_server.sabang.dto.BitbucketPrGetResponse;
import com.mcp_server.sabang.dto.BitbucketPrLineCommentRequest;
import com.mcp_server.sabang.service.BitbucketPrService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class BitbucketPrTools {

    private final BitbucketPrService bitbucketPrService;

    public BitbucketPrTools(BitbucketPrService bitbucketPrService) {
        this.bitbucketPrService = bitbucketPrService;
    }

    @McpTool(name = "bitbucket-pr-get", description = "Get Bitbucket pull request details and changes", generateOutputSchema = true)
    public BitbucketPrGetResponse getPullRequest(BitbucketPrGetRequest request) {
        return this.bitbucketPrService.getPullRequest(request);
    }

    @McpTool(name = "bitbucket-pr-comment-post", description = "Post a general comment to a Bitbucket pull request", generateOutputSchema = true)
    public BitbucketPrCommentResponse postComment(BitbucketPrCommentRequest request) {
        return this.bitbucketPrService.postComment(request);
    }

    @McpTool(name = "bitbucket-pr-line-comment-post", description = "Post a line comment to a Bitbucket pull request", generateOutputSchema = true)
    public BitbucketPrCommentResponse postLineComment(BitbucketPrLineCommentRequest request) {
        return this.bitbucketPrService.postLineComment(request);
    }
}
