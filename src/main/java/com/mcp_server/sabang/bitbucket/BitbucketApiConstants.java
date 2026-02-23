package com.mcp_server.sabang.bitbucket;

/**
 * Bitbucket API 관련 상수 정의
 */
public final class BitbucketApiConstants {

    private BitbucketApiConstants() {
    }

    public static final String API_BASE_PATH = "/rest/api/latest";
    public static final String PROJECTS_PATH = API_BASE_PATH + "/projects/%s";
    public static final String REPOS_PATH = PROJECTS_PATH + "/repos/%s";
    public static final String PULL_REQUESTS_PATH = REPOS_PATH + "/pull-requests/%d";
    public static final String PR_CHANGES_PATH = PULL_REQUESTS_PATH + "/changes?limit=1000";
    public static final String PR_COMMITS_PATH = PULL_REQUESTS_PATH + "/commits?limit=1000";
    public static final String PR_COMMENTS_PATH = PULL_REQUESTS_PATH + "/comments";

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String SEGMENT_PROJECTS = "projects";
    public static final String SEGMENT_REPOS = "repos";
    public static final String SEGMENT_PULL_REQUESTS = "pull-requests";
}
