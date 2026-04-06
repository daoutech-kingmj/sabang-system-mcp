package com.mcp_server.sabang.dto;

import java.util.List;

public record JenniferSearchErrorsResponse(
    int domainId,
    int totalCount,
    List<ErrorInfo> errors
) {

    public record ErrorInfo(
        String instanceName,
        String errorType,
        String message,
        String applicationName,
        String txid,
        String time
    ) {
    }
}
