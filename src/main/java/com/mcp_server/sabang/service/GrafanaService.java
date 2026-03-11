package com.mcp_server.sabang.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcp_server.sabang.dto.GrafanaListDatasourcesRequest;
import com.mcp_server.sabang.dto.GrafanaListDatasourcesResponse;
import com.mcp_server.sabang.dto.GrafanaListDatasourcesResponse.DatasourceInfo;
import com.mcp_server.sabang.grafana.GrafanaApiClient;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

import static com.mcp_server.sabang.grafana.GrafanaApiConstants.*;

@Service
public class GrafanaService {

    private final GrafanaApiClient apiClient;

    public GrafanaService(GrafanaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public GrafanaListDatasourcesResponse listDatasources(GrafanaListDatasourcesRequest request) {
        JsonNode response = apiClient.get(DATASOURCES_PATH);

        List<DatasourceInfo> datasources = new ArrayList<>();
        if (response.isArray()) {
            for (JsonNode node : response) {
                datasources.add(new DatasourceInfo(
                    node.path("id").asInt(),
                    node.path("uid").asText(""),
                    node.path("name").asText(""),
                    node.path("type").asText("")
                ));
            }
        }

        return new GrafanaListDatasourcesResponse(datasources);
    }
}