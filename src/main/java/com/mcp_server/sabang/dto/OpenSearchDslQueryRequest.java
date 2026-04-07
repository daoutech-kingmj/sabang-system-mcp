package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record OpenSearchDslQueryRequest(
    @McpToolParam(description = "Target index name (e.g. prod-jeus-20260407)") String index,
    @McpToolParam(description = "OpenSearch Query DSL as JSON string. Supports full DSL features: bool queries, match_phrase, date_histogram, pipeline aggregations, search_after, etc.") String dslBody
) {

}
