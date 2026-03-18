package com.mcp_server.sabang.dto;

import java.util.Map;

public record PinpointGetTransactionDetailResponse(
    String traceId,
    Map<String, Object> transactionInfo
) {
}
