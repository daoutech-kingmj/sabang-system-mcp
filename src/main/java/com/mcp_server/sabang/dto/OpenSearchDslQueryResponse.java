package com.mcp_server.sabang.dto;

import java.util.List;
import java.util.Map;

public record OpenSearchDslQueryResponse(
    String index,
    int totalHits,
    List<Map<String, Object>> hits,
    Map<String, Object> aggregations
) {

}
