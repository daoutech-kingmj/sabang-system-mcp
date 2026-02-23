package com.mcp_server.sabang.gitlab;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/**
 * GitLab API 응답에서 필요한 필드를 추출하고 검증하는 유틸리티
 */
@Component
public class GitlabResponseValidator {

    /**
     * JSON 응답에서 필수 필드를 추출합니다.
     *
     * @param node JSON 노드
     * @param fieldName 추출할 필드명
     * @return 추출된 JSON 노드
     * @throws com.mcp_server.sabang.exception.GitlabApiException 필드가 없거나 null인 경우
     */
    public JsonNode requireField(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            throw new com.mcp_server.sabang.exception.GitlabApiException("GitLab API response missing required field: " + fieldName);
        }
        return fieldNode;
    }
}
