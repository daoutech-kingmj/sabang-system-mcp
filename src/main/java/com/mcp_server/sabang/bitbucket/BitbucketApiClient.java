package com.mcp_server.sabang.bitbucket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcp_server.sabang.exception.BitbucketApiException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.stereotype.Component;

import static com.mcp_server.sabang.bitbucket.BitbucketApiConstants.*;

/**
 * Bitbucket API와의 HTTP 통신을 담당하는 클라이언트
 */
@Component
public class BitbucketApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BitbucketApiClient(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public JsonNode get(String baseUrl, String path, String personalAccessToken) {
        HttpRequest request = HttpRequest.newBuilder(buildUri(baseUrl, path))
            .header(HEADER_AUTHORIZATION, buildBearerToken(personalAccessToken))
            .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
            .GET()
            .build();
        return sendRequest(request);
    }

    public JsonNode post(String baseUrl, String path, String personalAccessToken, ObjectNode body) {
        String payload = serializeRequestBody(body);
        HttpRequest request = HttpRequest.newBuilder(buildUri(baseUrl, path))
            .header(HEADER_AUTHORIZATION, buildBearerToken(personalAccessToken))
            .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
            .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();
        return sendRequest(request);
    }

    private String serializeRequestBody(ObjectNode body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (IOException ex) {
            throw new BitbucketApiException("Failed to serialize request body", ex);
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
            throw new BitbucketApiException("Bitbucket API request interrupted", ex);
        } catch (IOException ex) {
            throw new BitbucketApiException("Bitbucket API request failed", ex);
        }
    }

    private void validateResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            String errorMessage = String.format("Bitbucket API request failed with status %d: %s", statusCode, response.body());
            throw new BitbucketApiException(statusCode, errorMessage);
        }
    }

    private JsonNode parseResponse(HttpResponse<String> response) {
        try {
            return objectMapper.readTree(response.body());
        } catch (IOException ex) {
            throw new BitbucketApiException("Failed to parse Bitbucket API response", ex);
        }
    }

    private static URI buildUri(String baseUrl, String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return URI.create(baseUrl + normalizedPath);
    }

    private static String buildBearerToken(String token) {
        return "Bearer " + token;
    }
}
