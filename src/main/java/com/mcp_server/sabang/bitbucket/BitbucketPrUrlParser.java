package com.mcp_server.sabang.bitbucket;

import com.mcp_server.sabang.exception.InvalidPrUrlException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

import static com.mcp_server.sabang.bitbucket.BitbucketApiConstants.*;

/**
 * Bitbucket Pull Request URL을 파싱하여 필요한 정보를 추출하는 파서
 * URL 형식 예시: https://bitbucket.example.com/projects/PROJ/repos/my-repo/pull-requests/123
 */
@Component
public class BitbucketPrUrlParser {

    public BitbucketPrRef parse(String prUrl) {
        validatePrUrl(prUrl);

        URI uri = parseUri(prUrl);
        String baseUrl = buildBaseUrl(uri);
        List<String> segments = extractPathSegments(uri.getPath());

        int projectsIndex = segments.indexOf(SEGMENT_PROJECTS);
        validateSegmentStructure(segments, projectsIndex, prUrl);

        String projectKey = segments.get(projectsIndex + 1);
        String repoSlug = segments.get(projectsIndex + 3);
        int pullRequestId = extractPullRequestId(segments.get(projectsIndex + 5), prUrl);

        return new BitbucketPrRef(baseUrl, projectKey, repoSlug, pullRequestId);
    }

    private void validatePrUrl(String prUrl) {
        if (prUrl == null || prUrl.isBlank()) {
            throw new InvalidPrUrlException("PR URL is required and cannot be blank");
        }
    }

    private URI parseUri(String prUrl) {
        try {
            URI uri = URI.create(prUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new InvalidPrUrlException("Invalid PR URL format (missing scheme or host): " + prUrl);
            }
            return uri;
        } catch (IllegalArgumentException ex) {
            throw new InvalidPrUrlException("Failed to parse PR URL: " + prUrl, ex);
        }
    }

    private void validateSegmentStructure(List<String> segments, int projectsIndex, String prUrl) {
        if (projectsIndex < 0 || segments.size() <= projectsIndex + 5) {
            throw new InvalidPrUrlException("Invalid PR URL: insufficient segments at " + prUrl);
        }

        if (!SEGMENT_REPOS.equals(segments.get(projectsIndex + 2))) {
            throw new InvalidPrUrlException("Invalid PR URL: expected '" + SEGMENT_REPOS + "' segment at " + prUrl);
        }

        if (!SEGMENT_PULL_REQUESTS.equals(segments.get(projectsIndex + 4))) {
            throw new InvalidPrUrlException("Invalid PR URL: expected '" + SEGMENT_PULL_REQUESTS + "' segment at " + prUrl);
        }
    }

    private int extractPullRequestId(String pullRequestIdString, String prUrl) {
        try {
            return Integer.parseInt(pullRequestIdString);
        } catch (NumberFormatException ex) {
            throw new InvalidPrUrlException("Invalid PR URL: pull request ID is not a number ('" + pullRequestIdString + "') at " + prUrl, ex);
        }
    }

    private static String buildBaseUrl(URI uri) {
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(uri.getScheme())
            .append("://")
            .append(uri.getHost());

        if (uri.getPort() != -1) {
            baseUrl.append(":").append(uri.getPort());
        }

        return baseUrl.toString();
    }

    private static List<String> extractPathSegments(String path) {
        if (path == null || path.isEmpty()) {
            return new ArrayList<>();
        }

        String[] rawSegments = path.split("/");
        List<String> segments = new ArrayList<>();

        for (String segment : rawSegments) {
            if (segment != null && !segment.isBlank()) {
                segments.add(segment);
            }
        }

        return segments;
    }
}
