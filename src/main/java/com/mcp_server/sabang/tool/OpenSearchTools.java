package com.mcp_server.sabang.tool;

import com.mcp_server.sabang.dto.OpenSearchGetMappingRequest;
import com.mcp_server.sabang.dto.OpenSearchGetMappingResponse;
import com.mcp_server.sabang.dto.OpenSearchListIndicesRequest;
import com.mcp_server.sabang.dto.OpenSearchListIndicesResponse;
import com.mcp_server.sabang.dto.OpenSearchSqlQueryRequest;
import com.mcp_server.sabang.dto.OpenSearchSqlQueryResponse;
import com.mcp_server.sabang.service.OpenSearchService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class OpenSearchTools {

    private final OpenSearchService openSearchService;

    public OpenSearchTools(OpenSearchService openSearchService) {
        this.openSearchService = openSearchService;
    }

    @McpTool(name = "opensearch-sql-query", description = "Execute an OpenSearch SQL query via the _plugins/_sql API. Use standard SQL syntax with index names as table names.", generateOutputSchema = true)
    public OpenSearchSqlQueryResponse sqlQuery(OpenSearchSqlQueryRequest request) {
        return openSearchService.executeSqlQuery(request);
    }

    @McpTool(name = "opensearch-list-indices", description = "List OpenSearch indices. Optionally filter by pattern (e.g. work-notification*).", generateOutputSchema = true)
    public OpenSearchListIndicesResponse listIndices(OpenSearchListIndicesRequest request) {
        return openSearchService.listIndices(request);
    }

    @McpTool(name = "opensearch-get-mapping", description = "Get field mapping (schema) of an OpenSearch index. Useful to understand available fields before querying.", generateOutputSchema = true)
    public OpenSearchGetMappingResponse getMapping(OpenSearchGetMappingRequest request) {
        return openSearchService.getMapping(request);
    }
}