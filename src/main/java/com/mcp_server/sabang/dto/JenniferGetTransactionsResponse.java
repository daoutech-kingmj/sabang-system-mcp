package com.mcp_server.sabang.dto;

import java.util.List;

public record JenniferGetTransactionsResponse(
    int domainId,
    int totalCount,
    List<TransactionSummary> transactions
) {

    public record TransactionSummary(
        String txid,
        String instanceName,
        String applicationName,
        String clientIp,
        long startTime,
        long endTime,
        long responseTime,
        long cpuTime,
        long sqlTime,
        long externalcallTime,
        String errorType
    ) {
    }
}
