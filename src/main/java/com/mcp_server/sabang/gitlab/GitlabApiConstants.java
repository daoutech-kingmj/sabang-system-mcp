package com.mcp_server.sabang.gitlab;

/**
 * GitLab API 관련 상수 정의
 */
public final class GitlabApiConstants {

    private GitlabApiConstants() {
    }

    // API 버전
    public static final String API_VERSION = "v4";

    // API 경로 템플릿
    public static final String API_BASE_PATH = "/api/" + API_VERSION;
    public static final String PROJECTS_PATH = API_BASE_PATH + "/projects/%s";
    public static final String MERGE_REQUESTS_PATH = PROJECTS_PATH + "/merge_requests/%d";
    public static final String MR_CHANGES_PATH = MERGE_REQUESTS_PATH + "/changes";
    public static final String MR_VERSIONS_PATH = MERGE_REQUESTS_PATH + "/versions";
    public static final String MR_NOTES_PATH = MERGE_REQUESTS_PATH + "/notes";
    public static final String MR_DISCUSSIONS_PATH = MERGE_REQUESTS_PATH + "/discussions";

    // HTTP 헤더
    public static final String HEADER_PRIVATE_TOKEN = "PRIVATE-TOKEN";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";

    // MR URL 세그먼트
    public static final String SEGMENT_SEPARATOR = "-";
    public static final String SEGMENT_MERGE_REQUESTS = "merge_requests";
}
