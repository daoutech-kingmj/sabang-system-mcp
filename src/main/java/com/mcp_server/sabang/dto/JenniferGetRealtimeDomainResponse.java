package com.mcp_server.sabang.dto;

import java.util.List;

public record JenniferGetRealtimeDomainResponse(
    List<RealtimeDomainData> domains
) {

    public record RealtimeDomainData(
        int domainId,
        String domainName,
        int activeService,
        int activeUser,
        double tps,
        double responseTime,
        double concurrentUser,
        double rejectRate,
        long hitHour,
        long hitDay,
        long visitHour,
        long visitDay
    ) {
    }
}
