package com.mcp_server.sabang.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mcp_server.sabang.dto.OpenSearchGetMappingRequest;
import com.mcp_server.sabang.dto.OpenSearchGetMappingResponse;
import com.mcp_server.sabang.dto.OpenSearchListIndicesRequest;
import com.mcp_server.sabang.dto.OpenSearchListIndicesResponse;
import com.mcp_server.sabang.dto.OpenSearchListIndicesResponse.IndexInfo;
import com.mcp_server.sabang.dto.OpenSearchDslQueryRequest;
import com.mcp_server.sabang.dto.OpenSearchDslQueryResponse;
import com.mcp_server.sabang.dto.OpenSearchSqlQueryRequest;
import com.mcp_server.sabang.dto.OpenSearchSqlQueryResponse;
import com.mcp_server.sabang.opensearch.OpenSearchApiClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public OpenSearchDslQueryResponse executeDslQuery(OpenSearchDslQueryRequest request) {
        String path = String.format(SEARCH_PATH, request.index());
        String correctedDslBody = correctTextFieldsToKeyword(request.index(), request.dslBody());
        JsonNode response = apiClient.post(path, correctedDslBody);

        int totalHits = 0;
        List<Map<String, Object>> hits = new ArrayList<>();
        Map<String, Object> aggregations = Map.of();

        JsonNode hitsNode = response.path("hits");
        if (!hitsNode.isMissingNode()) {
            JsonNode totalNode = hitsNode.path("total");
            if (totalNode.has("value")) {
                totalHits = totalNode.get("value").asInt();
            }

            for (JsonNode hit : hitsNode.path("hits")) {
                Map<String, Object> hitMap = new LinkedHashMap<>();
                hitMap.put("_id", hit.path("_id").asText());
                if (hit.has("_source")) {
                    hitMap.put("_source", objectMapper.convertValue(hit.get("_source"), Map.class));
                }
                if (hit.has("highlight")) {
                    hitMap.put("highlight", objectMapper.convertValue(hit.get("highlight"), Map.class));
                }
                hits.add(hitMap);
            }
        }

        JsonNode aggsNode = response.path("aggregations");
        if (!aggsNode.isMissingNode()) {
            aggregations = objectMapper.convertValue(aggsNode, Map.class);
        }

        return new OpenSearchDslQueryResponse(request.index(), totalHits, hits, aggregations);
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

    /**
     * text 필드에 .keyword 서브필드가 있으면 aggregation/sort에서 자동 보정
     * sort 필드가 매핑에 없으면 대체 날짜/시간 필드를 자동 탐색
     */
    private String correctTextFieldsToKeyword(String index, String dslBody) {
        try {
            JsonNode dsl = objectMapper.readTree(dslBody);
            if (!dsl.isObject()) return dslBody;

            Set<String> textFieldsWithKeyword = getTextFieldsWithKeyword(index);
            Map<String, String> allFields = getAllFieldsWithType(index);

            ObjectNode dslObj = (ObjectNode) dsl;
            boolean changed = false;

            // aggregation 보정
            if (!textFieldsWithKeyword.isEmpty()) {
                if (dslObj.has("aggs")) {
                    changed |= correctFieldsInAggs(dslObj.get("aggs"), textFieldsWithKeyword);
                }
                if (dslObj.has("aggregations")) {
                    changed |= correctFieldsInAggs(dslObj.get("aggregations"), textFieldsWithKeyword);
                }
            }

            // sort 보정: text→keyword 변환 + 누락 필드 대체
            if (dslObj.has("sort")) {
                if (!textFieldsWithKeyword.isEmpty()) {
                    changed |= correctFieldsInSort(dslObj, textFieldsWithKeyword);
                }
                if (!allFields.isEmpty()) {
                    changed |= replaceMissingSortFields(dslObj, allFields);
                }
            }

            return changed ? objectMapper.writeValueAsString(dslObj) : dslBody;
        } catch (Exception e) {
            // 보정 실패 시 원본 그대로 전달
            return dslBody;
        }
    }

    /**
     * 매핑에서 text 타입이면서 keyword 서브필드를 가진 필드 목록 반환
     */
    private Set<String> getTextFieldsWithKeyword(String index) {
        try {
            String mappingPath = String.format(MAPPING_PATH, index);
            JsonNode mappingResponse = apiClient.get(mappingPath);
            JsonNode properties = mappingResponse.path(index).path("mappings").path("properties");
            if (properties.isMissingNode()) return Set.of();

            Set<String> result = new HashSet<>();
            collectTextFieldsWithKeyword(properties, "", result);
            return result;
        } catch (Exception e) {
            return Set.of();
        }
    }

    private void collectTextFieldsWithKeyword(JsonNode properties, String prefix, Set<String> result) {
        var fields = properties.fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            String fieldName = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            JsonNode fieldDef = entry.getValue();

            if ("text".equals(fieldDef.path("type").asText(null))
                    && fieldDef.has("fields")
                    && fieldDef.path("fields").has("keyword")) {
                result.add(fieldName);
            }

            // 중첩 properties 탐색
            if (fieldDef.has("properties")) {
                collectTextFieldsWithKeyword(fieldDef.get("properties"), fieldName, result);
            }
        }
    }

    /**
     * aggregation 내 field 값이 text 필드면 .keyword로 보정
     */
    private boolean correctFieldsInAggs(JsonNode aggsNode, Set<String> textFields) {
        if (!aggsNode.isObject()) return false;
        boolean changed = false;

        var aggEntries = aggsNode.fields();
        while (aggEntries.hasNext()) {
            var aggEntry = aggEntries.next();
            JsonNode aggDef = aggEntry.getValue();

            // 각 aggregation 타입(terms, cardinality, avg, min, max 등)의 field 보정
            var typedEntries = aggDef.fields();
            while (typedEntries.hasNext()) {
                var typedEntry = typedEntries.next();
                String key = typedEntry.getKey();
                JsonNode typedNode = typedEntry.getValue();

                if ("aggs".equals(key) || "aggregations".equals(key)) {
                    changed |= correctFieldsInAggs(typedNode, textFields);
                } else if (typedNode.isObject() && typedNode.has("field")) {
                    String fieldValue = typedNode.get("field").asText();
                    if (textFields.contains(fieldValue)) {
                        ((ObjectNode) typedNode).put("field", fieldValue + ".keyword");
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    /**
     * 매핑에서 모든 필드명과 타입을 수집
     */
    private Map<String, String> getAllFieldsWithType(String index) {
        try {
            String mappingPath = String.format(MAPPING_PATH, index);
            JsonNode mappingResponse = apiClient.get(mappingPath);
            JsonNode properties = mappingResponse.path(index).path("mappings").path("properties");
            if (properties.isMissingNode()) return Map.of();

            Map<String, String> result = new LinkedHashMap<>();
            collectAllFields(properties, "", result);
            return result;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private void collectAllFields(JsonNode properties, String prefix, Map<String, String> result) {
        var fields = properties.fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            String fieldName = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            JsonNode fieldDef = entry.getValue();
            String type = fieldDef.path("type").asText(null);
            if (type != null) {
                result.put(fieldName, type);
            }
            if (fieldDef.has("properties")) {
                collectAllFields(fieldDef.get("properties"), fieldName, result);
            }
        }
    }

    /**
     * sort에 사용된 필드가 매핑에 없으면 대체 날짜/시간 필드로 교체
     */
    private boolean replaceMissingSortFields(ObjectNode dslObj, Map<String, String> allFields) {
        JsonNode sortNode = dslObj.get("sort");
        if (!sortNode.isArray()) return false;

        boolean changed = false;
        ArrayNode sortArray = (ArrayNode) sortNode;

        for (int i = 0; i < sortArray.size(); i++) {
            JsonNode sortItem = sortArray.get(i);
            String sortFieldName = null;

            if (sortItem.isTextual()) {
                sortFieldName = sortItem.asText();
            } else if (sortItem.isObject()) {
                var it = sortItem.fieldNames();
                if (it.hasNext()) sortFieldName = it.next();
            }

            if (sortFieldName == null || allFields.containsKey(sortFieldName)) continue;
            // .keyword 접미사가 붙은 경우 원본 필드명으로도 확인
            if (sortFieldName.endsWith(".keyword")
                    && allFields.containsKey(sortFieldName.substring(0, sortFieldName.length() - 8))) continue;

            String fallback = findFallbackTimestampField(allFields);
            if (fallback == null) continue;

            if (sortItem.isTextual()) {
                sortArray.set(i, new TextNode(fallback));
                changed = true;
            } else if (sortItem.isObject()) {
                ObjectNode sortObj = (ObjectNode) sortItem;
                JsonNode value = sortObj.remove(sortFieldName);
                sortObj.set(fallback, value);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * 대체 날짜/시간 필드 탐색: date 타입 우선, 없으면 이름 기반 탐색
     */
    private String findFallbackTimestampField(Map<String, String> allFields) {
        // 1순위: date 타입 필드
        for (var entry : allFields.entrySet()) {
            if ("date".equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        // 2순위: 이름에 timestamp/created/time 포함된 keyword 서브필드
        for (var entry : allFields.entrySet()) {
            String name = entry.getKey().toLowerCase();
            if ((name.contains("timestamp") || name.contains("created") || name.contains("time"))
                    && "text".equals(entry.getValue())) {
                return entry.getKey() + ".keyword";
            }
        }
        return null;
    }

    /**
     * sort 배열 내 text 필드를 .keyword로 보정
     */
    private boolean correctFieldsInSort(ObjectNode dslObj, Set<String> textFields) {
        JsonNode sortNode = dslObj.get("sort");
        if (!sortNode.isArray()) return false;

        boolean changed = false;
        ArrayNode sortArray = (ArrayNode) sortNode;

        for (int i = 0; i < sortArray.size(); i++) {
            JsonNode sortItem = sortArray.get(i);

            if (sortItem.isTextual()) {
                // "fieldName" 형태
                String fieldName = sortItem.asText();
                if (textFields.contains(fieldName)) {
                    sortArray.set(i, new TextNode(fieldName + ".keyword"));
                    changed = true;
                }
            } else if (sortItem.isObject()) {
                // {"fieldName": {"order": "desc"}} 형태
                ObjectNode sortObj = (ObjectNode) sortItem;
                List<String> fieldsToRename = new ArrayList<>();
                var sortFields = sortObj.fields();
                while (sortFields.hasNext()) {
                    var sf = sortFields.next();
                    if (textFields.contains(sf.getKey())) {
                        fieldsToRename.add(sf.getKey());
                    }
                }
                for (String f : fieldsToRename) {
                    JsonNode value = sortObj.remove(f);
                    sortObj.set(f + ".keyword", value);
                    changed = true;
                }
            }
        }
        return changed;
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