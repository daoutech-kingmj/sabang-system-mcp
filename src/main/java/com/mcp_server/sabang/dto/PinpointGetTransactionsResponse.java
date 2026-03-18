package com.mcp_server.sabang.dto;

import java.util.List;

public record PinpointGetTransactionsResponse(
    String applicationName,
    int totalCount,
    List<TransactionSummary> transactions
) {

    public record TransactionSummary(
        String traceId,
        long collectorAcceptTime,
        long elapsed,
        String endpoint,
        String agentId,
        int exception
    ) {
    }
}