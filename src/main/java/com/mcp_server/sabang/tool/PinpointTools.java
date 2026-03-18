package com.mcp_server.sabang.tool;

import com.mcp_server.sabang.dto.PinpointGetAgentStatRequest;
import com.mcp_server.sabang.dto.PinpointGetAgentStatResponse;
import com.mcp_server.sabang.dto.PinpointGetAgentsRequest;
import com.mcp_server.sabang.dto.PinpointGetAgentsResponse;
import com.mcp_server.sabang.dto.PinpointGetDetailedAgentInfoRequest;
import com.mcp_server.sabang.dto.PinpointGetDetailedAgentInfoResponse;
import com.mcp_server.sabang.dto.PinpointGetErrorTransactionsRequest;
import com.mcp_server.sabang.dto.PinpointGetErrorTransactionsResponse;
import com.mcp_server.sabang.dto.PinpointGetResponseHistogramRequest;
import com.mcp_server.sabang.dto.PinpointGetResponseHistogramResponse;
import com.mcp_server.sabang.dto.PinpointGetServerMapRequest;
import com.mcp_server.sabang.dto.PinpointGetServerMapResponse;
import com.mcp_server.sabang.dto.PinpointGetTransactionsRequest;
import com.mcp_server.sabang.dto.PinpointGetTransactionsResponse;
import com.mcp_server.sabang.dto.PinpointGetTransactionDetailRequest;
import com.mcp_server.sabang.dto.PinpointGetTransactionDetailResponse;
import com.mcp_server.sabang.dto.PinpointGetTransactionTimelineRequest;
import com.mcp_server.sabang.dto.PinpointGetTransactionTimelineResponse;
import com.mcp_server.sabang.dto.PinpointListApplicationsResponse;
import com.mcp_server.sabang.service.PinpointService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class PinpointTools {

    private final PinpointService pinpointService;

    public PinpointTools(PinpointService pinpointService) {
        this.pinpointService = pinpointService;
    }

    @McpTool(name = "pinpoint-list-applications", description = "List all applications registered in Pinpoint APM.", generateOutputSchema = true)
    public PinpointListApplicationsResponse listApplications() {
        return pinpointService.listApplications();
    }

    @McpTool(name = "pinpoint-get-agents", description = "Get agent list for a specific Pinpoint application. Returns agent IDs, hostnames, and IPs.", generateOutputSchema = true)
    public PinpointGetAgentsResponse getAgents(PinpointGetAgentsRequest request) {
        return pinpointService.getAgents(request);
    }

    @McpTool(name = "pinpoint-get-server-map", description = "Get server map data showing call relationships between services, including error rates and response time histograms.", generateOutputSchema = true)
    public PinpointGetServerMapResponse getServerMap(PinpointGetServerMapRequest request) {
        return pinpointService.getServerMap(request);
    }

    @McpTool(name = "pinpoint-get-transactions", description = "Get transactions for an application within a time range from heatmap scatter data. Results are sorted by collectorAcceptTime DESC (newest first). Use minResponseTime to filter by response time (e.g. 3000 for slow transactions). Each transaction has an 'exception' field (non-zero = root span error). Note: long-running error transactions may start much earlier than their collectorAcceptTime, so they can be pushed out by the limit.", generateOutputSchema = true)
    public PinpointGetTransactionsResponse getTransactions(PinpointGetTransactionsRequest request) {
        return pinpointService.getTransactions(request);
    }

    @McpTool(name = "pinpoint-get-error-transactions", description = "Get error transactions using histogram-based detection. Finds time windows with errors from response histogram, then checks each transaction's HTTP status code and exception info. Catches both Java exceptions AND HTTP 4xx/5xx errors that scatter data misses.", generateOutputSchema = true)
    public PinpointGetErrorTransactionsResponse getErrorTransactions(PinpointGetErrorTransactionsRequest request) {
        return pinpointService.getErrorTransactions(request);
    }

    @McpTool(name = "pinpoint-get-transaction-detail", description = "Get detailed transaction info including call stack, span list, exception messages, and SQL/HTTP annotations. Use a traceId obtained from error transactions or scatter data.", generateOutputSchema = true)
    public PinpointGetTransactionDetailResponse getTransactionDetail(PinpointGetTransactionDetailRequest request) {
        return pinpointService.getTransactionDetail(request);
    }

    @McpTool(name = "pinpoint-get-agent-stat", description = "Get agent infrastructure stats chart data. Chart types: jvmGc (heap/GC), cpuLoad (JVM/system CPU), activeTrace (active threads by speed), transaction, responseTime, dataSource, totalThreadCount, fileDescriptor.", generateOutputSchema = true)
    public PinpointGetAgentStatResponse getAgentStat(PinpointGetAgentStatRequest request) {
        return pinpointService.getAgentStat(request);
    }

    @McpTool(name = "pinpoint-get-transaction-timeline", description = "Get transaction timeline info showing visual span breakdown with start/end times for each call in the trace. Use a traceId obtained from transactions or scatter data.", generateOutputSchema = true)
    public PinpointGetTransactionTimelineResponse getTransactionTimeline(PinpointGetTransactionTimelineRequest request) {
        return pinpointService.getTransactionTimeline(request);
    }

    @McpTool(name = "pinpoint-get-detailed-agent-info", description = "Get detailed agent information including server metadata, JVM info, service type, and status. Provide current epoch milliseconds as timestamp.", generateOutputSchema = true)
    public PinpointGetDetailedAgentInfoResponse getDetailedAgentInfo(PinpointGetDetailedAgentInfoRequest request) {
        return pinpointService.getDetailedAgentInfo(request);
    }

    @McpTool(name = "pinpoint-get-response-histogram", description = "Get response time histogram data for an application. Shows distribution of response times (1s, 3s, 5s, Slow, Error) over time.", generateOutputSchema = true)
    public PinpointGetResponseHistogramResponse getResponseHistogram(PinpointGetResponseHistogramRequest request) {
        return pinpointService.getResponseHistogram(request);
    }
}
