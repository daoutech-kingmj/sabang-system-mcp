package com.mcp_server.sabang.jennifer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp_server.sabang.exception.JenniferApiException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.mcp_server.sabang.jennifer.JenniferApiConstants.*;

@Component
public class JenniferApiClient {

    private static final Logger log = LoggerFactory.getLogger(JenniferApiClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String token;

    public JenniferApiClient(
        ObjectMapper objectMapper,
        @Value("${jennifer.host:}") String host,
        @Value("${jennifer.token:}") String token
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.baseUrl = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
        this.token = token;
        log.info("JenniferApiClient initialized - host: [{}], baseUrl: [{}], token present: {}", host, this.baseUrl, !token.isBlank());
    }

    public JsonNode get(String path) {
        HttpRequest request = HttpRequest.newBuilder(buildUri(path))
            .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
            .header(HEADER_AUTHORIZATION, "Bearer " + token)
            .GET()
            .build();
        return sendRequest(request);
    }

    public String getText(String path) {
        HttpRequest request = HttpRequest.newBuilder(buildUri(path))
            .header(HEADER_AUTHORIZATION, "Bearer " + token)
            .GET()
            .build();
        HttpResponse<String> response = executeHttpRequest(request);
        validateResponse(response);
        return response.body();
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
            throw new JenniferApiException("Jennifer API request interrupted", ex);
        } catch (IOException ex) {
            throw new JenniferApiException("Jennifer API request failed", ex);
        }
    }

    private void validateResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            String errorMessage = String.format("Jennifer API failed with status %d: %s", statusCode, response.body());
            throw new JenniferApiException(statusCode, errorMessage);
        }
    }

    private JsonNode parseResponse(HttpResponse<String> response) {
        try {
            return objectMapper.readTree(response.body());
        } catch (IOException ex) {
            throw new JenniferApiException("Failed to parse Jennifer API response", ex);
        }
    }

    private URI buildUri(String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return URI.create(baseUrl + normalizedPath);
    }
}
