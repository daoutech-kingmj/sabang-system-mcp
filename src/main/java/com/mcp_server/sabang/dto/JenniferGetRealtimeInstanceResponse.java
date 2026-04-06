package com.mcp_server.sabang.dto;

import java.util.List;
import java.util.Map;

public record JenniferGetRealtimeInstanceResponse(
    int domainId,
    List<RealtimeInstanceData> instances
) {

    public record RealtimeInstanceData(
        int instanceId,
        String instanceName,
        int activeService,
        double tps,
        double responseTime,
        double concurrentUser,
        double procCPU,
        double procMemory,
        double heapCommitted,
        double heapUsed,
        Map<String, Double> serviceRateByRange,
        long hitHour,
        long hitDay
    ) {
    }
}
