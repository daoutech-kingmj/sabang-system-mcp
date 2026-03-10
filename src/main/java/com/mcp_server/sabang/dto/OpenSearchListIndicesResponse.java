package com.mcp_server.sabang.dto;

import java.util.List;

public record OpenSearchListIndicesResponse(
    List<IndexInfo> indices
) {

    public record IndexInfo(
        String index,
        String health,
        String status,
        String docsCount,
        String storeSize
    ) {

    }
}