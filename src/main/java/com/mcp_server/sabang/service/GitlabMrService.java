package com.mcp_server.sabang.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcp_server.sabang.dto.GitlabMrChange;
import com.mcp_server.sabang.dto.GitlabMrCommentRequest;
import com.mcp_server.sabang.dto.GitlabMrCommentResponse;
import com.mcp_server.sabang.dto.GitlabMrGetRequest;
import com.mcp_server.sabang.dto.GitlabMrGetResponse;
import com.mcp_server.sabang.dto.GitlabMrLineCommentRequest;
import com.mcp_server.sabang.dto.GitlabMrVersion;
import com.mcp_server.sabang.gitlab.GitlabApiClient;
import com.mcp_server.sabang.dto.GitlabMrRef;
import com.mcp_server.sabang.gitlab.GitlabMrUrlParser;
import com.mcp_server.sabang.gitlab.GitlabResponseValidator;
import java.util.List;
import org.springframework.stereotype.Service;

import static com.mcp_server.sabang.gitlab.GitlabApiConstants.*;

/**
 * GitLab Merge Request 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
public class GitlabMrService {

    private final GitlabMrUrlParser urlParser;
    private final GitlabApiClient apiClient;
    private final GitlabResponseValidator responseValidator;
    private final ObjectMapper objectMapper;

    public GitlabMrService(GitlabMrUrlParser urlParser, GitlabApiClient apiClient,
        GitlabResponseValidator responseValidator, ObjectMapper objectMapper) {
        this.urlParser = urlParser;
        this.apiClient = apiClient;
        this.responseValidator = responseValidator;
        this.objectMapper = objectMapper;
    }

    /**
     * Merge Request의 상세 정보와 변경사항을 조회합니다.
     *
     * @param request MR 조회 요청 (URL과 액세스 토큰 포함)
     * @return MR 상세 정보와 변경사항
     */
    public GitlabMrGetResponse getMergeRequest(GitlabMrGetRequest request) {
        GitlabMrRef mergeRequestRef = urlParser.parse(request.mrUrl());
        // API 경로 생성
        String changesPath = buildMergeRequestChangesPath(mergeRequestRef.projectIdEnc(), mergeRequestRef.iid());
        String versionsPath = buildMergeRequestVersionsPath(mergeRequestRef.projectIdEnc(), mergeRequestRef.iid());
        // GitLab API 호출
        JsonNode changesResponse = apiClient.get(mergeRequestRef.baseUrl(), changesPath, request.pat());
        JsonNode versionsResponse = apiClient.get(mergeRequestRef.baseUrl(), versionsPath, request.pat());
        // 응답 파싱
        List<GitlabMrChange> changes = parseChanges(changesResponse);
        List<GitlabMrVersion> versions = parseVersions(versionsResponse);

        return new GitlabMrGetResponse(
            request.mrUrl(),
            mergeRequestRef.baseUrl(),
            mergeRequestRef.projectPath(),
            mergeRequestRef.projectIdEnc(),
            mergeRequestRef.iid(),
            changes,
            versions
        );
    }

    /**
     * Merge Request에 댓글을 작성합니다.
     *
     * @param request 댓글 작성 요청 (MR URL, 액세스 토큰, 댓글 내용 포함)
     * @return 작성된 댓글 정보
     */
    public GitlabMrCommentResponse postComment(GitlabMrCommentRequest request) {
        GitlabMrRef mergeRequestRef = urlParser.parse(request.mrUrl());
        // API 경로 생성
        String notesPath = buildMergeRequestNotesPath(mergeRequestRef.projectIdEnc(), mergeRequestRef.iid());
        // 요청 본문 생성
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("body", request.body());
        // GitLab API 호출
        JsonNode response = apiClient.post(mergeRequestRef.baseUrl(), notesPath, request.pat(), requestBody);
        // 응답 파싱
        long noteId = responseValidator.requireField(response, "id").asLong();
        // GitLab Notes API는 web_url을 반환하지 않으므로 MR URL과 note ID로 직접 생성
        String webUrl = buildNoteWebUrl(request.mrUrl(), noteId);

        return new GitlabMrCommentResponse(request.mrUrl(), noteId, webUrl);
    }

    /**
     * Merge Request의 특정 라인에 인라인 댓글을 작성합니다.
     *
     * @param request 인라인 댓글 작성 요청
     * @return 작성된 댓글 정보
     */
    public GitlabMrCommentResponse postLineComment(GitlabMrLineCommentRequest request) {
        GitlabMrRef mergeRequestRef = urlParser.parse(request.mrUrl());
        String discussionsPath = buildMergeRequestDiscussionsPath(mergeRequestRef.projectIdEnc(), mergeRequestRef.iid());
        String versionsPath = buildMergeRequestVersionsPath(mergeRequestRef.projectIdEnc(), mergeRequestRef.iid());
        JsonNode versionsResponse = apiClient.get(mergeRequestRef.baseUrl(), versionsPath, request.pat());
        GitlabMrVersion latestVersion = extractLatestVersion(versionsResponse);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("body", request.body());

        ObjectNode position = requestBody.putObject("position");
        position.put("position_type", "text");
        position.put("base_sha", latestVersion.baseCommitSha());
        position.put("start_sha", latestVersion.startCommitSha());
        position.put("head_sha", latestVersion.headCommitSha());
        position.put("old_path", request.path());
        position.put("new_path", request.path());

        if ("REMOVED".equals(normalizeLineType(request.lineType()))) {
            position.put("old_line", request.line());
        } else {
            position.put("new_line", request.line());
        }

        JsonNode response = apiClient.post(mergeRequestRef.baseUrl(), discussionsPath, request.pat(), requestBody);
        JsonNode notes = responseValidator.requireField(response, "notes");
        if (!notes.isArray() || notes.isEmpty()) {
            throw new com.mcp_server.sabang.exception.GitlabApiException("GitLab API response missing discussion notes");
        }
        long noteId = responseValidator.requireField(notes.get(0), "id").asLong();
        String webUrl = buildNoteWebUrl(request.mrUrl(), noteId);

        return new GitlabMrCommentResponse(request.mrUrl(), noteId, webUrl);
    }

    // ===== Private Helper Methods =====
    private List<GitlabMrChange> parseChanges(JsonNode changesResponse) {
        JsonNode changesNode = responseValidator.requireField(changesResponse, "changes");
        return objectMapper.convertValue(changesNode, new TypeReference<>() {});
    }

    private List<GitlabMrVersion> parseVersions(JsonNode versionsResponse) {
        return objectMapper.convertValue(versionsResponse, new TypeReference<>() {});
    }

    private GitlabMrVersion extractLatestVersion(JsonNode versionsResponse) {
        List<GitlabMrVersion> versions = parseVersions(versionsResponse);
        if (versions.isEmpty()) {
            throw new com.mcp_server.sabang.exception.GitlabApiException("GitLab MR has no versions for inline comment");
        }

        GitlabMrVersion latestVersion = versions.get(0);
        if (latestVersion.baseCommitSha() == null || latestVersion.baseCommitSha().isBlank()
            || latestVersion.startCommitSha() == null || latestVersion.startCommitSha().isBlank()
            || latestVersion.headCommitSha() == null || latestVersion.headCommitSha().isBlank()) {
            throw new com.mcp_server.sabang.exception.GitlabApiException("GitLab MR version is missing required commit SHAs");
        }
        return latestVersion;
    }

    private String normalizeLineType(String lineType) {
        if (lineType == null || lineType.isBlank()) {
            return "ADDED";
        }
        return "REMOVED".equalsIgnoreCase(lineType) ? "REMOVED" : "ADDED";
    }

    private String buildMergeRequestChangesPath(String encodedProjectId, int mergeRequestIid) {
        return String.format(MR_CHANGES_PATH, encodedProjectId, mergeRequestIid);
    }

    private String buildMergeRequestVersionsPath(String encodedProjectId, int mergeRequestIid) {
        return String.format(MR_VERSIONS_PATH, encodedProjectId, mergeRequestIid);
    }

    private String buildMergeRequestNotesPath(String encodedProjectId, int mergeRequestIid) {
        return String.format(MR_NOTES_PATH, encodedProjectId, mergeRequestIid);
    }

    private String buildMergeRequestDiscussionsPath(String encodedProjectId, int mergeRequestIid) {
        return String.format(MR_DISCUSSIONS_PATH, encodedProjectId, mergeRequestIid);
    }

    private String buildNoteWebUrl(String mrUrl, long noteId) {
        // GitLab의 댓글 URL 형식: {MR_URL}#note_{NOTE_ID}
        return mrUrl + "#note_" + noteId;
    }
}

