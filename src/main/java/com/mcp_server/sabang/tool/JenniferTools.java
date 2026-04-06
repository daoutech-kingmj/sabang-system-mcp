package com.mcp_server.sabang.tool;

import com.mcp_server.sabang.dto.JenniferGetActiveServicesRequest;
import com.mcp_server.sabang.dto.JenniferGetActiveServicesResponse;
import com.mcp_server.sabang.dto.JenniferGetInstanceMetricsRequest;
import com.mcp_server.sabang.dto.JenniferGetInstanceMetricsResponse;
import com.mcp_server.sabang.dto.JenniferGetInstancesRequest;
import com.mcp_server.sabang.dto.JenniferGetInstancesResponse;
import com.mcp_server.sabang.dto.JenniferGetRealtimeDomainRequest;
import com.mcp_server.sabang.dto.JenniferGetRealtimeDomainResponse;
import com.mcp_server.sabang.dto.JenniferGetRealtimeInstanceRequest;
import com.mcp_server.sabang.dto.JenniferGetRealtimeInstanceResponse;
import com.mcp_server.sabang.dto.JenniferGetTransactionDetailRequest;
import com.mcp_server.sabang.dto.JenniferGetTransactionDetailResponse;
import com.mcp_server.sabang.dto.JenniferGetTransactionProfileRequest;
import com.mcp_server.sabang.dto.JenniferGetTransactionProfileResponse;
import com.mcp_server.sabang.dto.JenniferGetTransactionsRequest;
import com.mcp_server.sabang.dto.JenniferGetTransactionsResponse;
import com.mcp_server.sabang.dto.JenniferListDomainsResponse;
import com.mcp_server.sabang.dto.JenniferSearchErrorsRequest;
import com.mcp_server.sabang.dto.JenniferSearchErrorsResponse;
import com.mcp_server.sabang.service.JenniferService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class JenniferTools {

    private final JenniferService jenniferService;

    public JenniferTools(JenniferService jenniferService) {
        this.jenniferService = jenniferService;
    }

    @McpTool(name = "jennifer-list-domains", description = "List all domains registered in Jennifer APM. Returns domain IDs, names, group hierarchy, and instance counts (total/live/stopped).", generateOutputSchema = true)
    public JenniferListDomainsResponse listDomains() {
        return jenniferService.listDomains();
    }

    @McpTool(name = "jennifer-get-instances", description = "Get instance (agent) list for a specific Jennifer domain. Returns instance IDs, names, versions, IPs, hostnames, and status (LIVE/STOPPED).", generateOutputSchema = true)
    public JenniferGetInstancesResponse getInstances(JenniferGetInstancesRequest request) {
        return jenniferService.getInstances(request);
    }

    @McpTool(name = "jennifer-get-realtime-domain", description = "Get real-time domain metrics including TPS, response time, active services, concurrent users, hit/visit counts. Use domain_id=0 for all domains.", generateOutputSchema = true)
    public JenniferGetRealtimeDomainResponse getRealtimeDomain(JenniferGetRealtimeDomainRequest request) {
        return jenniferService.getRealtimeDomain(request);
    }

    @McpTool(name = "jennifer-get-realtime-instance", description = "Get real-time instance metrics including CPU usage, heap memory, TPS, response time, active services. Use instance_id=0 for all instances in the domain.", generateOutputSchema = true)
    public JenniferGetRealtimeInstanceResponse getRealtimeInstance(JenniferGetRealtimeInstanceRequest request) {
        return jenniferService.getRealtimeInstance(request);
    }

    @McpTool(name = "jennifer-get-active-services", description = "Get currently running active services (in-flight requests) for a domain. Shows real-time processing status, elapsed time, SQL/external call details.", generateOutputSchema = true)
    public JenniferGetActiveServicesResponse getActiveServices(JenniferGetActiveServicesRequest request) {
        return jenniferService.getActiveServices(request);
    }

    @McpTool(name = "jennifer-get-transactions", description = "Get transactions (X-View) for a domain within a time range. Time range is limited to 1 minute. Returns transaction IDs, application names, response times, SQL times, error types.", generateOutputSchema = true)
    public JenniferGetTransactionsResponse getTransactions(JenniferGetTransactionsRequest request) {
        return jenniferService.getTransactions(request);
    }

    @McpTool(name = "jennifer-get-transaction-detail", description = "Get transaction detail by transaction ID. Returns instance name, application name, response/CPU/SQL times, and error type.", generateOutputSchema = true)
    public JenniferGetTransactionDetailResponse getTransactionDetail(JenniferGetTransactionDetailRequest request) {
        return jenniferService.getTransactionDetail(request);
    }

    @McpTool(name = "jennifer-get-transaction-profile", description = "Get transaction call profile as text. Shows the full call tree with method names, SQL statements, execution times, and thread information. Use txid and time from transaction list.", generateOutputSchema = true)
    public JenniferGetTransactionProfileResponse getTransactionProfile(JenniferGetTransactionProfileRequest request) {
        return jenniferService.getTransactionProfile(request);
    }

    @McpTool(name = "jennifer-search-errors", description = "Search errors in Jennifer APM for a domain within a time range. Returns error types (SQL_TOOMANY_FETCH, EXCEPTION, etc.), messages, application names, and transaction IDs.", generateOutputSchema = true)
    public JenniferSearchErrorsResponse searchErrors(JenniferSearchErrorsRequest request) {
        return jenniferService.searchErrors(request);
    }

    @McpTool(name = "jennifer-get-instance-metrics", description = "Get historical instance metrics data. Metrics: proc_cpu, sys_cpu, heap_used, heap_committed, nonheap_used, gc_time, gc_count, thread_current, active_service, tps, response_time, service_count, service_err_count, error_count.", generateOutputSchema = true)
    public JenniferGetInstanceMetricsResponse getInstanceMetrics(JenniferGetInstanceMetricsRequest request) {
        return jenniferService.getInstanceMetrics(request);
    }
}
