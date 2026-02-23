package com.mcp_server.sabang.gitlab;

import com.mcp_server.sabang.dto.GitlabMrRef;
import com.mcp_server.sabang.exception.InvalidMrUrlException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

import static com.mcp_server.sabang.gitlab.GitlabApiConstants.*;

/**
 * GitLab Merge Request URL을 파싱하여 필요한 정보를 추출하는 파서
 * <p>
 * URL 형식 예시: https://gitlab.com/group/project/-/merge_requests/123
 */
@Component
public class GitlabMrUrlParser {

    private static final int MINIMUM_SEGMENTS_BEFORE_SEPARATOR = 1;

    /**
     * MR URL을 파싱하여 GitlabMrRef 객체로 변환합니다.
     *
     * @param mrUrl 파싱할 MR URL
     * @return GitLab MR 참조 정보
     * @throws InvalidMrUrlException URL이 유효하지 않은 경우
     */
    public GitlabMrRef parse(String mrUrl) {
        validateMrUrl(mrUrl);

        URI uri = parseUri(mrUrl);
        String baseUrl = buildBaseUrl(uri);
        List<String> segments = extractPathSegments(uri.getPath());

        int separatorIndex = findSeparatorIndex(segments, mrUrl);
        validateSegmentStructure(segments, separatorIndex, mrUrl);

        String projectPath = extractProjectPath(segments, separatorIndex);
        int mergeRequestId = extractMergeRequestId(segments, separatorIndex, mrUrl);
        String encodedProjectId = encodeProjectPath(projectPath);

        return new GitlabMrRef(baseUrl, projectPath, encodedProjectId, mergeRequestId);
    }

    private void validateMrUrl(String mrUrl) {
        if (mrUrl == null || mrUrl.isBlank()) {
            throw new InvalidMrUrlException("MR URL is required and cannot be blank");
        }
    }

    private URI parseUri(String mrUrl) {
        try {
            URI uri = URI.create(mrUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new InvalidMrUrlException("Invalid MR URL format (missing scheme or host): " + mrUrl);
            }
            return uri;
        } catch (IllegalArgumentException ex) {
            throw new InvalidMrUrlException("Failed to parse MR URL: " + mrUrl, ex);
        }
    }

    private int findSeparatorIndex(List<String> segments, String mrUrl) {
        int index = segments.indexOf(SEGMENT_SEPARATOR);
        if (index < MINIMUM_SEGMENTS_BEFORE_SEPARATOR) {
            throw new InvalidMrUrlException("Invalid MR URL: missing separator '" + SEGMENT_SEPARATOR + "' segment at " + mrUrl);
        }
        return index;
    }

    private void validateSegmentStructure(List<String> segments, int separatorIndex, String mrUrl) {
        int requiredSegmentsAfterSeparator = 2; // "merge_requests" + IID

        if (segments.size() <= separatorIndex + requiredSegmentsAfterSeparator) {
            throw new InvalidMrUrlException("Invalid MR URL: insufficient segments after separator at " + mrUrl);
        }

        String mergeRequestsSegment = segments.get(separatorIndex + 1);
        if (!SEGMENT_MERGE_REQUESTS.equals(mergeRequestsSegment)) {
            throw new InvalidMrUrlException("Invalid MR URL: expected '" + SEGMENT_MERGE_REQUESTS + "' but found '"
                + mergeRequestsSegment + "' at " + mrUrl);
        }
    }

    private String extractProjectPath(List<String> segments, int separatorIndex) {
        return String.join("/", segments.subList(0, separatorIndex));
    }

    private int extractMergeRequestId(List<String> segments, int separatorIndex, String mrUrl) {
        String mergeRequestIdString = segments.get(separatorIndex + 2);
        try {
            return Integer.parseInt(mergeRequestIdString);
        } catch (NumberFormatException ex) {
            throw new InvalidMrUrlException("Invalid MR URL: merge request ID is not a number ('" + mergeRequestIdString + "') at " + mrUrl, ex);
        }
    }

    private String encodeProjectPath(String projectPath) {
        return URLEncoder.encode(projectPath, StandardCharsets.UTF_8);
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

