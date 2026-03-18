package com.mcp_server.sabang.dto;

import java.util.List;

public record PinpointListApplicationsResponse(
    List<ApplicationInfo> applications
) {

    public record ApplicationInfo(
        String applicationName,
        String serviceType,
        int code
    ) {
    }
}
