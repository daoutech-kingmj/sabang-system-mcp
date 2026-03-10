package com.mcp_server.sabang.dto;

import java.util.Map;

public record OpenSearchGetMappingResponse(
    String index,
    Map<String, Object> mapping
) {

}