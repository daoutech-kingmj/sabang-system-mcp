package com.mcp_server.sabang.pinpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp_server.sabang.exception.PinpointApiException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.mcp_server.sabang.pinpoint.PinpointApiConstants.*;

@Component
public class PinpointApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public PinpointApiClient(
        ObjectMapper objectMapper,
        @Value("${pinpoint.host:}") String host
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.baseUrl = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
    }

    public JsonNode get(String path) {
        HttpRequest request = HttpRequest.newBuilder(buildUri(path))
            .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
            .GET()
            .build();
        return sendRequest(request);
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
            throw new PinpointApiException("Pinpoint API request interrupted", ex);
        } catch (IOException ex) {
            throw new PinpointApiException("Pinpoint API request failed", ex);
        }
    }

    private void validateResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            String errorMessage = String.format("Pinpoint API failed with status %d: %s", statusCode, response.body());
            throw new PinpointApiException(statusCode, errorMessage);
        }
    }

    private JsonNode parseResponse(HttpResponse<String> response) {
        try {
            return objectMapper.readTree(response.body());
        } catch (IOException ex) {
            throw new PinpointApiException("Failed to parse Pinpoint API response", ex);
        }
    }

    private URI buildUri(String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return URI.create(baseUrl + normalizedPath);
    }
}
