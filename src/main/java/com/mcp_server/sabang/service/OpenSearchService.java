package com.mcp_server.sabang.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp_server.sabang.dto.OpenSearchGetMappingRequest;
import com.mcp_server.sabang.dto.OpenSearchGetMappingResponse;
import com.mcp_server.sabang.dto.OpenSearchListIndicesRequest;
import com.mcp_server.sabang.dto.OpenSearchListIndicesResponse;
import com.mcp_server.sabang.dto.OpenSearchListIndicesResponse.IndexInfo;
import com.mcp_server.sabang.dto.OpenSearchSqlQueryRequest;
import com.mcp_server.sabang.dto.OpenSearchSqlQueryResponse;
import com.mcp_server.sabang.opensearch.OpenSearchApiClient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

import static com.mcp_server.sabang.opensearch.OpenSearchApiConstants.*;

@Service
public class OpenSearchService {

    private final OpenSearchApiClient apiClient;
    private final ObjectMapper objectMapper;

    public OpenSearchService(OpenSearchApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    public OpenSearchSqlQueryResponse executeSqlQuery(OpenSearchSqlQueryRequest request) {
        String body = "{\"query\":\"" + escapeJson(request.query()) + "\"}";
        JsonNode response = apiClient.post(SQL_PATH, body);

        List<Map<String, Object>> rows = new ArrayList<>();
        int totalHits = 0;

        if (response.has("schema") && response.has("datarows")) {
            JsonNode schema = response.get("schema");
            JsonNode datarows = response.get("datarows");
            totalHits = response.has("total") ? response.get("total").asInt() : datarows.size();

            List<String> columns = new ArrayList<>();
            for (JsonNode col : schema) {
                columns.add(col.get("name").asText());
            }

            for (JsonNode row : datarows) {
                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < columns.size(); i++) {
                    JsonNode value = row.get(i);
                    rowMap.put(columns.get(i), value.isNull() ? null : value.isNumber() ? value.numberValue() : value.asText());
                }
                rows.add(rowMap);
            }
        }

        return new OpenSearchSqlQueryResponse(request.query(), rows, totalHits);
    }

    public OpenSearchListIndicesResponse listIndices(OpenSearchListIndicesRequest request) {
        String path = CAT_INDICES_PATH;
        if (request.pattern() != null && !request.pattern().isBlank()) {
            path = "/_cat/indices/" + request.pattern() + "?format=json&h=index,health,status,docs.count,store.size";
        }

        JsonNode response = apiClient.get(path);

        List<IndexInfo> indices = new ArrayList<>();
        if (response.isArray()) {
            for (JsonNode node : response) {
                indices.add(new IndexInfo(
                    node.path("index").asText(""),
                    node.path("health").asText(""),
                    node.path("status").asText(""),
                    node.path("docs.count").asText("0"),
                    node.path("store.size").asText("0")
                ));
            }
        }

        return new OpenSearchListIndicesResponse(indices);
    }

    @SuppressWarnings("unchecked")
    public OpenSearchGetMappingResponse getMapping(OpenSearchGetMappingRequest request) {
        String path = String.format(MAPPING_PATH, request.index());
        JsonNode response = apiClient.get(path);

        Map<String, Object> mapping = Map.of();
        JsonNode indexNode = response.path(request.index()).path("mappings");
        if (!indexNode.isMissingNode()) {
            mapping = objectMapper.convertValue(indexNode, Map.class);
        }

        return new OpenSearchGetMappingResponse(request.index(), mapping);
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}