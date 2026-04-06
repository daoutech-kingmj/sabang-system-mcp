package com.mcp_server.sabang.dto;

import org.springaicommunity.mcp.annotation.McpToolParam;

public record JenniferGetInstanceMetricsRequest(
    @McpToolParam(description = "Domain ID (e.g. 3000 for Staging)") int domainId,
    @McpToolParam(description = "Instance ID (e.g. 10000)") int instanceId,
    @McpToolParam(description = "Start time in epoch milliseconds") long startTime,
    @McpToolParam(description = "End time in epoch milliseconds") long endTime,
    @McpToolParam(description = "Interval in minutes for data aggregation (e.g. 1, 5, 10, 30, 60)") int intervalMinute,
    @McpToolParam(description = "Comma-separated metric names. Instance metrics: proc_cpu, sys_cpu, heap_used, heap_committed, nonheap_used, gc_time, gc_count, thread_current, thread_daemon, active_service, tps, response_time, service_count, service_err_count, error_count") String metrics
) {
}
