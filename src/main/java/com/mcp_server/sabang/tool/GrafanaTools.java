package com.mcp_server.sabang.tool;

import com.mcp_server.sabang.dto.GrafanaListDatasourcesRequest;
import com.mcp_server.sabang.dto.GrafanaListDatasourcesResponse;
import com.mcp_server.sabang.service.GrafanaService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class GrafanaTools {

    private final GrafanaService grafanaService;

    public GrafanaTools(GrafanaService grafanaService) {
        this.grafanaService = grafanaService;
    }

    @McpTool(name = "grafana-list-datasources", description = "List all Grafana datasources. Use this first to find the numeric datasource ID needed for Loki queries.", generateOutputSchema = true)
    public GrafanaListDatasourcesResponse listDatasources(GrafanaListDatasourcesRequest request) {
        return grafanaService.listDatasources(request);
    }
}