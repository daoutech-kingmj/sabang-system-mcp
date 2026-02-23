package com.mcp_server.sabang.bitbucket;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcp_server.sabang.exception.BitbucketApiException;
import org.springframework.stereotype.Component;

/**
 * Bitbucket API 응답에서 필요한 필드를 추출하고 검증하는 유틸리티
 */
@Component
public class BitbucketResponseValidator {

    public JsonNode requireField(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            throw new BitbucketApiException("Bitbucket API response missing required field: " + fieldName);
        }
        return fieldNode;
    }
}
