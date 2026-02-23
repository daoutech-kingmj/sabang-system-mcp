package com.mcp_server.sabang.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcp_server.sabang.bitbucket.BitbucketApiClient;
import com.mcp_server.sabang.bitbucket.BitbucketPrRef;
import com.mcp_server.sabang.bitbucket.BitbucketPrUrlParser;
import com.mcp_server.sabang.bitbucket.BitbucketResponseValidator;
import com.mcp_server.sabang.dto.BitbucketPrChange;
import com.mcp_server.sabang.dto.BitbucketPrCommentRequest;
import com.mcp_server.sabang.dto.BitbucketPrCommentResponse;
import com.mcp_server.sabang.dto.BitbucketPrGetRequest;
import com.mcp_server.sabang.dto.BitbucketPrGetResponse;
import com.mcp_server.sabang.dto.BitbucketPrLineCommentRequest;
import com.mcp_server.sabang.dto.BitbucketPrVersion;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

import static com.mcp_server.sabang.bitbucket.BitbucketApiConstants.*;

/**
 * Bitbucket Pull Request 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
public class BitbucketPrService {

    private final BitbucketPrUrlParser urlParser;
    private final BitbucketApiClient apiClient;
    private final BitbucketResponseValidator responseValidator;
    private final ObjectMapper objectMapper;

    public BitbucketPrService(
        BitbucketPrUrlParser urlParser,
        BitbucketApiClient apiClient,
        BitbucketResponseValidator responseValidator,
        ObjectMapper objectMapper
    ) {
        this.urlParser = urlParser;
        this.apiClient = apiClient;
        this.responseValidator = responseValidator;
        this.objectMapper = objectMapper;
    }

    public BitbucketPrGetResponse getPullRequest(BitbucketPrGetRequest request) {
        BitbucketPrRef pullRequestRef = urlParser.parse(request.prUrl());

        String changesPath = buildPullRequestChangesPath(pullRequestRef.projectKey(), pullRequestRef.repoSlug(), pullRequestRef.id());
        String commitsPath = buildPullRequestCommitsPath(pullRequestRef.projectKey(), pullRequestRef.repoSlug(), pullRequestRef.id());

        JsonNode changesResponse = apiClient.get(pullRequestRef.baseUrl(), changesPath, request.pat());
        JsonNode commitsResponse = apiClient.get(pullRequestRef.baseUrl(), commitsPath, request.pat());

        List<BitbucketPrChange> changes = parseChanges(changesResponse);
        List<BitbucketPrVersion> versions = parseVersions(commitsResponse);

        return new BitbucketPrGetResponse(
            request.prUrl(),
            pullRequestRef.baseUrl(),
            pullRequestRef.projectKey(),
            pullRequestRef.repoSlug(),
            pullRequestRef.id(),
            changes,
            versions
        );
    }

    public BitbucketPrCommentResponse postComment(BitbucketPrCommentRequest request) {
        BitbucketPrRef pullRequestRef = urlParser.parse(request.prUrl());

        String commentsPath = buildPullRequestCommentsPath(pullRequestRef.projectKey(), pullRequestRef.repoSlug(), pullRequestRef.id());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("text", request.body());

        JsonNode response = apiClient.post(pullRequestRef.baseUrl(), commentsPath, request.pat(), requestBody);
        long commentId = responseValidator.requireField(response, "id").asLong();
        String webUrl = response.path("links").path("self").path(0).path("href").asText(buildCommentWebUrl(request.prUrl(), commentId));

        return new BitbucketPrCommentResponse(request.prUrl(), commentId, webUrl);
    }

    public BitbucketPrCommentResponse postLineComment(BitbucketPrLineCommentRequest request) {
        BitbucketPrRef pullRequestRef = urlParser.parse(request.prUrl());

        String commentsPath = buildPullRequestCommentsPath(pullRequestRef.projectKey(), pullRequestRef.repoSlug(), pullRequestRef.id());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("text", request.body());

        ObjectNode anchor = requestBody.putObject("anchor");
        anchor.put("diffType", "EFFECTIVE");
        anchor.put("path", request.path());
        anchor.put("line", request.line());
        anchor.put("lineType", normalizeLineType(request.lineType()));
        anchor.put("fileType", "TO");

        JsonNode response = apiClient.post(pullRequestRef.baseUrl(), commentsPath, request.pat(), requestBody);
        long commentId = responseValidator.requireField(response, "id").asLong();
        String webUrl = response.path("links").path("self").path(0).path("href").asText(buildCommentWebUrl(request.prUrl(), commentId));

        return new BitbucketPrCommentResponse(request.prUrl(), commentId, webUrl);
    }

    private List<BitbucketPrChange> parseChanges(JsonNode changesResponse) {
        JsonNode values = responseValidator.requireField(changesResponse, "values");
        List<BitbucketPrChange> changes = new ArrayList<>();

        for (JsonNode change : values) {
            String path = change.path("path").path("toString").asText("");
            String type = change.path("type").asText("");
            String nodeType = change.path("nodeType").asText("");
            boolean executable = change.path("executable").asBoolean(false);
            changes.add(new BitbucketPrChange(path, type, nodeType, executable));
        }

        return changes;
    }

    private List<BitbucketPrVersion> parseVersions(JsonNode commitsResponse) {
        JsonNode values = responseValidator.requireField(commitsResponse, "values");
        List<BitbucketPrVersion> versions = new ArrayList<>();

        for (JsonNode commit : values) {
            String id = commit.path("id").asText("");
            String displayId = commit.path("displayId").asText("");
            long authorTimestamp = commit.path("authorTimestamp").asLong(0L);
            String message = commit.path("message").asText("");
            versions.add(new BitbucketPrVersion(id, displayId, authorTimestamp, message));
        }

        return versions;
    }

    private String normalizeLineType(String lineType) {
        if (lineType == null || lineType.isBlank()) {
            return "ADDED";
        }

        String upperLineType = lineType.toUpperCase();
        if ("ADDED".equals(upperLineType) || "REMOVED".equals(upperLineType) || "CONTEXT".equals(upperLineType)) {
            return upperLineType;
        }

        return "ADDED";
    }

    private String buildPullRequestChangesPath(String projectKey, String repoSlug, int pullRequestId) {
        return String.format(PR_CHANGES_PATH, projectKey, repoSlug, pullRequestId);
    }

    private String buildPullRequestCommitsPath(String projectKey, String repoSlug, int pullRequestId) {
        return String.format(PR_COMMITS_PATH, projectKey, repoSlug, pullRequestId);
    }

    private String buildPullRequestCommentsPath(String projectKey, String repoSlug, int pullRequestId) {
        return String.format(PR_COMMENTS_PATH, projectKey, repoSlug, pullRequestId);
    }

    private String buildCommentWebUrl(String prUrl, long commentId) {
        return prUrl + "?commentId=" + commentId;
    }
}
