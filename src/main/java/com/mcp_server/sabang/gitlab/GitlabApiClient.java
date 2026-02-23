package com.mcp_server.sabang.gitlab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcp_server.sabang.exception.GitlabApiException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.stereotype.Component;

import static com.mcp_server.sabang.gitlab.GitlabApiConstants.*;

/**
 * GitLab API와의 HTTP 통신을 담당하는 클라이언트 GET, POST 요청을 처리하고 응답을 JsonNode로 반환합니다.
 */
@Component
public class GitlabApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GitlabApiClient(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    /**
     * GitLab API에 GET 요청을 보냅니다.
     *
     * @param baseUrl             GitLab 인스턴스의 기본 URL
     * @param path                API 경로
     * @param personalAccessToken 개인 액세스 토큰
     * @return JSON 응답
     */
    public JsonNode get(String baseUrl, String path, String personalAccessToken) {
        HttpRequest request = buildGetRequest(baseUrl, path, personalAccessToken);
        return sendRequest(request);
    }

    /**
     * GitLab API에 POST 요청을 보냅니다.
     *
     * @param baseUrl             GitLab 인스턴스의 기본 URL
     * @param path                API 경로
     * @param personalAccessToken 개인 액세스 토큰
     * @param body                요청 본문
     * @return JSON 응답
     */
    public JsonNode post(String baseUrl, String path, String personalAccessToken, ObjectNode body) {
        String payload = serializeRequestBody(body);
        HttpRequest request = buildPostRequest(baseUrl, path, personalAccessToken, payload);
        return sendRequest(request);
    }

    private HttpRequest buildGetRequest(String baseUrl, String path, String token) {
        return HttpRequest.newBuilder(buildUri(baseUrl, path))
            .header(HEADER_PRIVATE_TOKEN, token)
            .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
            .GET()
            .build();
    }

    private HttpRequest buildPostRequest(String baseUrl, String path, String token, String payload) {
        return HttpRequest.newBuilder(buildUri(baseUrl, path))
            .header(HEADER_PRIVATE_TOKEN, token)
            .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
            .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();
    }

    private String serializeRequestBody(ObjectNode body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (IOException ex) {
            throw new GitlabApiException("Failed to serialize request body", ex);
        }
    }

    private JsonNode sendRequest(HttpRequest request) {
        HttpResponse<String> response = executeHttpRequest(request);
        validateResponse(response);
        return parseResponse(response);
    }

    private HttpResponse<String> executeHttpRequest(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new GitlabApiException("GitLab API request interrupted", ex);
        } catch (IOException ex) {
            throw new GitlabApiException("GitLab API request failed", ex);
        }
    }

    private void validateResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            String errorMessage = String.format("GitLab API request failed with status %d: %s", statusCode, response.body());
            throw new GitlabApiException(statusCode, errorMessage);
        }
    }

    private JsonNode parseResponse(HttpResponse<String> response) {
        try {
            return objectMapper.readTree(response.body());
        } catch (IOException ex) {
            throw new GitlabApiException("Failed to parse GitLab API response", ex);
        }
    }

    private static URI buildUri(String baseUrl, String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return URI.create(baseUrl + normalizedPath);
    }
}

