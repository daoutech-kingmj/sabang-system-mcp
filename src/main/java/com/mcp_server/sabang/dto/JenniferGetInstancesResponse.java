package com.mcp_server.sabang.dto;

import java.util.List;

public record JenniferGetInstancesResponse(
    int domainId,
    List<InstanceInfo> instances
) {

    public record InstanceInfo(
        int instanceId,
        String name,
        String version,
        String ipAddress,
        String hostName,
        String platform,
        String status
    ) {
    }
}
