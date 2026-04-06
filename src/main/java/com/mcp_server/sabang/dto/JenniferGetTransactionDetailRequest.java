package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record JenniferGetTransactionDetailRequest(
    @McpToolParam(description = "Domain ID (e.g. 3000 for Staging)") int domainId,
    @McpToolParam(description = "Transaction ID obtained from transaction list") String txid,
    @McpToolParam(description = "Transaction start time in epoch milliseconds") long time
) {
}
