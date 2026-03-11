package com.mcp_server.sabang.dto;

import java.util.List;

public record GrafanaListDatasourcesResponse(List<DatasourceInfo> datasources) {

    public record DatasourceInfo(int id, String uid, String name, String type) {
    }
}