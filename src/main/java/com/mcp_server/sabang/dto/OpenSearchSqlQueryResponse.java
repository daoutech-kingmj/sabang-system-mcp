package com.mcp_server.sabang.dto;

import java.util.List;
import java.util.Map;

public record OpenSearchSqlQueryResponse(
    String query,
    List<Map<String, Object>> rows,
    int totalHits
) {

}