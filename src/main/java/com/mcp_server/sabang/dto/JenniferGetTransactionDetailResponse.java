package com.mcp_server.sabang.dto;

import java.util.List;

public record JenniferGetTransactionDetailResponse(
    String txid,
    List<TransactionDetail> transactions
) {

    public record TransactionDetail(
        String instanceName,
        String applicationName,
        String clientIp,
        long responseTime,
        long cpuTime,
        long sqlTime,
        long externalcallTime,
        String errorType
    ) {
    }
}
