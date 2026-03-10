package com.mcp_server.sabang.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp_server.sabang.exception.OpenSearchApiException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.mcp_server.sabang.opensearch.OpenSearchApiConstants.*;

@Component
public class OpenSearchApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String authHeader;

    public OpenSearchApiClient(
        ObjectMapper objectMapper,
        @Value("${opensearch.host:}") String host,
        @Value("${opensearch.username:}") String username,
        @Value("${opensearch.password:}") String password
    ) {
        this.httpClient = HttpClient.newBuilder()
            .sslContext(createTrustAllSslContext())
            .build();
        this.objectMapper = objectMapper;
        this.baseUrl = host.startsWith("http") ? host : "https://" + host;
        String credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        this.authHeader = "Basic " + credentials;
    }

    public JsonNode get(String path) {
        HttpRequest request = HttpRequest.newBuilder(buildUri(path))
            .header(HEADER_AUTHORIZATION, authHeader)
            .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
            .GET()
            .build();
        return sendRequest(request);
    }

    public JsonNode post(String path, String body) {
        HttpRequest request = HttpRequest.newBuilder(buildUri(path))
            .header(HEADER_AUTHORIZATION, authHeader)
            .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
            .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
            .POST(HttpRequest.BodyPublishers.ofString(body))
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
            throw new OpenSearchApiException("OpenSearch API request interrupted", ex);
        } catch (IOException ex) {
            throw new OpenSearchApiException("OpenSearch API request failed", ex);
        }
    }

    private void validateResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            String errorMessage = String.format("OpenSearch API failed with status %d: %s", statusCode, response.body());
            throw new OpenSearchApiException(statusCode, errorMessage);
        }
    }

    private JsonNode parseResponse(HttpResponse<String> response) {
        try {
            return objectMapper.readTree(response.body());
        } catch (IOException ex) {
            throw new OpenSearchApiException("Failed to parse OpenSearch API response", ex);
        }
    }

    private URI buildUri(String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return URI.create(baseUrl + normalizedPath);
    }

    private static SSLContext createTrustAllSslContext() {
        try {
            TrustManager[] trustAll = { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAll, new java.security.SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create trust-all SSL context", e);
        }
    }
}