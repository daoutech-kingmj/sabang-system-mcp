package com.mcp_server.sabang.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp_server.sabang.dto.PinpointGetAgentStatRequest;
import com.mcp_server.sabang.dto.PinpointGetAgentStatResponse;
import com.mcp_server.sabang.dto.PinpointGetAgentsRequest;
import com.mcp_server.sabang.dto.PinpointGetAgentsResponse;
import com.mcp_server.sabang.dto.PinpointGetAgentsResponse.AgentInfo;
import com.mcp_server.sabang.dto.PinpointGetDetailedAgentInfoRequest;
import com.mcp_server.sabang.dto.PinpointGetDetailedAgentInfoResponse;
import com.mcp_server.sabang.dto.PinpointGetErrorTransactionsRequest;
import com.mcp_server.sabang.dto.PinpointGetErrorTransactionsResponse;
import com.mcp_server.sabang.dto.PinpointGetErrorTransactionsResponse.TransactionSummary;
import com.mcp_server.sabang.dto.PinpointGetResponseHistogramRequest;
import com.mcp_server.sabang.dto.PinpointGetResponseHistogramResponse;
import com.mcp_server.sabang.dto.PinpointGetTransactionsRequest;
import com.mcp_server.sabang.dto.PinpointGetTransactionsResponse;
import com.mcp_server.sabang.dto.PinpointGetServerMapRequest;
import com.mcp_server.sabang.dto.PinpointGetServerMapResponse;
import com.mcp_server.sabang.dto.PinpointGetTransactionDetailRequest;
import com.mcp_server.sabang.dto.PinpointGetTransactionDetailResponse;
import com.mcp_server.sabang.dto.PinpointGetTransactionTimelineRequest;
import com.mcp_server.sabang.dto.PinpointGetTransactionTimelineResponse;
import com.mcp_server.sabang.dto.PinpointListApplicationsResponse;
import com.mcp_server.sabang.dto.PinpointListApplicationsResponse.ApplicationInfo;
import com.mcp_server.sabang.pinpoint.PinpointApiClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

import static com.mcp_server.sabang.pinpoint.PinpointApiConstants.*;

@Service
public class PinpointService {

    private final PinpointApiClient apiClient;
    private final ObjectMapper objectMapper;

    public PinpointService(PinpointApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    public PinpointListApplicationsResponse listApplications() {
        JsonNode response = apiClient.get(APPLICATIONS_PATH);

        List<ApplicationInfo> applications = new ArrayList<>();
        if (response.isArray()) {
            for (JsonNode node : response) {
                applications.add(new ApplicationInfo(
                    node.path("applicationName").asText(""),
                    node.path("serviceType").asText(""),
                    node.path("code").asInt(0)
                ));
            }
        }

        return new PinpointListApplicationsResponse(applications);
    }

    public PinpointGetAgentsResponse getAgents(PinpointGetAgentsRequest request) {
        String path = AGENT_LIST_PATH + "?application=" + encode(request.applicationName());
        JsonNode response = apiClient.get(path);

        List<AgentInfo> agents = new ArrayList<>();
        var fields = response.fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            JsonNode agentList = entry.getValue();
            if (agentList.isArray()) {
                for (JsonNode agent : agentList) {
                    agents.add(new AgentInfo(
                        agent.path("agentId").asText(""),
                        agent.path("hostName").asText(""),
                        agent.path("ip").asText(""),
                        agent.path("serviceType").asText("")
                    ));
                }
            }
        }

        return new PinpointGetAgentsResponse(request.applicationName(), agents);
    }

    @SuppressWarnings("unchecked")
    public PinpointGetServerMapResponse getServerMap(PinpointGetServerMapRequest request) {
        String path = String.format(
            "%s?applicationName=%s&serviceTypeCode=%d&from=%d&to=%d&callerRange=4&calleeRange=4",
            SERVER_MAP_PATH,
            encode(request.applicationName()),
            request.serviceTypeCode(),
            request.from(),
            request.to()
        );
        JsonNode response = apiClient.get(path);

        Map<String, Object> serverMapData = objectMapper.convertValue(response, Map.class);
        return new PinpointGetServerMapResponse(request.applicationName(), serverMapData);
    }

    public PinpointGetErrorTransactionsResponse getErrorTransactions(PinpointGetErrorTransactionsRequest request) {
        int limit = request.limit() != null ? request.limit() : 50;

        // 1. Look up serviceTypeName from application list
        String serviceTypeName = resolveServiceTypeName(request.applicationName());
        if (serviceTypeName == null) {
            return new PinpointGetErrorTransactionsResponse(request.applicationName(), 0, List.of());
        }

        // 2. Get histogram time series to find error windows
        String histPath = String.format(
            "%s?applicationName=%s&serviceTypeName=%s&from=%d&to=%d",
            RESPONSE_HISTOGRAM_PATH,
            encode(request.applicationName()),
            encode(serviceTypeName),
            request.from(),
            request.to()
        );
        JsonNode histResponse = apiClient.get(histPath);

        // 3. Extract time windows where errors occurred
        List<ErrorWindow> errorWindows = extractErrorTimeWindows(
            histResponse.path("timeSeries"), request.agentId()
        );
        if (errorWindows.isEmpty()) {
            return new PinpointGetErrorTransactionsResponse(request.applicationName(), 0, List.of());
        }

        // 4. For each error window, get transactions and check details
        List<TransactionSummary> errorTransactions = new ArrayList<>();
        for (ErrorWindow window : errorWindows) {
            if (errorTransactions.size() >= limit) break;

            String txPath = String.format(
                "%s?application=%s&x1=%d&x2=%d&y1=0&y2=600000&limit=200",
                HEATMAP_DRAG_PATH,
                encode(request.applicationName()),
                window.from(),
                window.to()
            );
            if (request.agentId() != null && !request.agentId().isBlank()) {
                txPath += "&agentId=" + encode(request.agentId());
            }
            JsonNode txResponse = apiClient.get(txPath);

            JsonNode metadata = txResponse.path("metadata");
            if (!metadata.isArray()) continue;

            // Split by exception flag — process flagged ones first (no detail call needed to confirm)
            List<JsonNode> withException = new ArrayList<>();
            List<JsonNode> withoutException = new ArrayList<>();
            for (JsonNode tx : metadata) {
                if (tx.path("exception").asInt(0) != 0) {
                    withException.add(tx);
                } else {
                    withoutException.add(tx);
                }
            }

            int foundInWindow = 0;

            // Process exception-flagged transactions first
            for (JsonNode tx : withException) {
                if (errorTransactions.size() >= limit) break;
                foundInWindow++;
                addErrorTransaction(errorTransactions, tx);
            }

            // Then check remaining transactions for HTTP status errors
            for (JsonNode tx : withoutException) {
                if (foundInWindow >= window.expectedErrorCount()) break;
                if (errorTransactions.size() >= limit) break;

                String traceId = tx.path("traceId").asText("");
                JsonNode detail = fetchTransactionDetail(traceId);

                int httpStatus = extractHttpStatusCode(detail);
                if (httpStatus >= 400) {
                    foundInWindow++;
                    String[] exInfo = extractExceptionInfo(detail);
                    errorTransactions.add(new TransactionSummary(
                        traceId,
                        tx.path("collectorAcceptTime").asLong(0),
                        tx.path("elapsed").asLong(0),
                        tx.path("endpoint").asText(""),
                        tx.path("agentId").asText(""),
                        httpStatus,
                        exInfo[0],
                        exInfo[1],
                        detail.path("applicationName").asText("")
                    ));
                }
            }
        }

        return new PinpointGetErrorTransactionsResponse(
            request.applicationName(),
            errorTransactions.size(),
            errorTransactions
        );
    }

    private void addErrorTransaction(List<TransactionSummary> list, JsonNode tx) {
        String traceId = tx.path("traceId").asText("");
        JsonNode detail = fetchTransactionDetail(traceId);
        int httpStatus = extractHttpStatusCode(detail);
        String[] exInfo = extractExceptionInfo(detail);
        list.add(new TransactionSummary(
            traceId,
            tx.path("collectorAcceptTime").asLong(0),
            tx.path("elapsed").asLong(0),
            tx.path("endpoint").asText(""),
            tx.path("agentId").asText(""),
            httpStatus > 0 ? httpStatus : 500,
            exInfo[0],
            exInfo[1],
            detail.path("applicationName").asText("")
        ));
    }

    private String resolveServiceTypeName(String applicationName) {
        JsonNode response = apiClient.get(APPLICATIONS_PATH);
        if (response.isArray()) {
            for (JsonNode node : response) {
                if (applicationName.equals(node.path("applicationName").asText())) {
                    return node.path("serviceType").asText(null);
                }
            }
        }
        return null;
    }

    private record ErrorWindow(long from, long to, int expectedErrorCount) {}

    private List<ErrorWindow> extractErrorTimeWindows(JsonNode timeSeries, String agentId) {
        List<ErrorWindow> windows = new ArrayList<>();
        var agents = timeSeries.fields();
        while (agents.hasNext()) {
            var entry = agents.next();
            String agent = entry.getKey();
            if (agentId != null && !agentId.isBlank() && !agent.equals(agentId)) continue;

            JsonNode agentSeries = entry.getValue();
            if (!agentSeries.isArray()) continue;

            for (JsonNode series : agentSeries) {
                if (!"Error".equals(series.path("key").asText())) continue;

                JsonNode values = series.path("values");
                if (!values.isArray()) continue;

                for (int i = 0; i < values.size(); i++) {
                    JsonNode point = values.get(i);
                    long ts = point.get(0).asLong();
                    int count = point.get(1).asInt();
                    if (count > 0) {
                        long nextTs = (i + 1 < values.size())
                            ? values.get(i + 1).get(0).asLong()
                            : ts + 300000;
                        windows.add(new ErrorWindow(ts, nextTs, count));
                    }
                }
            }
        }
        return windows;
    }

    private JsonNode fetchTransactionDetail(String traceId) {
        String path = String.format(
            "%s?traceId=%s&focusTimestamp=0",
            TRANSACTION_INFO_PATH,
            encode(traceId)
        );
        return apiClient.get(path);
    }

    private int extractHttpStatusCode(JsonNode detail) {
        JsonNode callStackIndex = detail.path("callStackIndex");
        int titleIdx = callStackIndex.path("title").asInt(-1);
        int argsIdx = callStackIndex.path("arguments").asInt(-1);
        if (titleIdx < 0 || argsIdx < 0) return 0;

        JsonNode callStack = detail.path("callStack");
        if (!callStack.isArray()) return 0;

        for (JsonNode entry : callStack) {
            if (!entry.isArray() || entry.size() <= Math.max(titleIdx, argsIdx)) continue;
            if ("http.status.code".equals(entry.get(titleIdx).asText(""))) {
                try {
                    return Integer.parseInt(entry.get(argsIdx).asText("0"));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private String[] extractExceptionInfo(JsonNode detail) {
        JsonNode callStackIndex = detail.path("callStackIndex");
        int titleIdx = callStackIndex.path("title").asInt(-1);
        int argsIdx = callStackIndex.path("arguments").asInt(-1);
        int hasExceptionIdx = callStackIndex.path("hasException").asInt(-1);
        if (titleIdx < 0 || argsIdx < 0 || hasExceptionIdx < 0) return new String[]{"", ""};

        JsonNode callStack = detail.path("callStack");
        if (!callStack.isArray()) return new String[]{"", ""};

        int maxIdx = Math.max(Math.max(titleIdx, argsIdx), hasExceptionIdx);
        for (JsonNode entry : callStack) {
            if (!entry.isArray() || entry.size() <= maxIdx) continue;
            if (entry.get(hasExceptionIdx).asBoolean(false)) {
                return new String[]{
                    entry.get(titleIdx).asText(""),
                    entry.get(argsIdx).asText("")
                };
            }
        }
        return new String[]{"", ""};
    }

    public PinpointGetTransactionsResponse getTransactions(PinpointGetTransactionsRequest request) {
        int limit = request.limit() != null ? request.limit() : 50;
        int y1 = request.minResponseTime() != null ? request.minResponseTime() : 0;
        int y2 = request.maxResponseTime() != null ? request.maxResponseTime() : 600000;
        String path = String.format(
            "%s?application=%s&x1=%d&x2=%d&y1=%d&y2=%d&limit=%d",
            HEATMAP_DRAG_PATH,
            encode(request.applicationName()),
            request.from(),
            request.to(),
            y1,
            y2,
            limit
        );
        if (request.agentId() != null && !request.agentId().isBlank()) {
            path += "&agentId=" + encode(request.agentId());
        }
        JsonNode response = apiClient.get(path);

        List<PinpointGetTransactionsResponse.TransactionSummary> transactions = new ArrayList<>();
        JsonNode metadata = response.path("metadata");
        if (metadata.isArray()) {
            for (JsonNode tx : metadata) {
                transactions.add(new PinpointGetTransactionsResponse.TransactionSummary(
                    tx.path("traceId").asText(""),
                    tx.path("collectorAcceptTime").asLong(0),
                    tx.path("elapsed").asLong(0),
                    tx.path("endpoint").asText(""),
                    tx.path("agentId").asText(""),
                    tx.path("exception").asInt(0)
                ));
            }
        }

        return new PinpointGetTransactionsResponse(
            request.applicationName(),
            transactions.size(),
            transactions
        );
    }

    @SuppressWarnings("unchecked")
    public PinpointGetTransactionDetailResponse getTransactionDetail(PinpointGetTransactionDetailRequest request) {
        long focusTimestamp = request.focusTimestamp() != null ? request.focusTimestamp() : 0L;
        String path = String.format(
            "%s?traceId=%s&focusTimestamp=%d",
            TRANSACTION_INFO_PATH,
            encode(request.traceId()),
            focusTimestamp
        );
        JsonNode response = apiClient.get(path);

        Map<String, Object> transactionInfo = objectMapper.convertValue(response, Map.class);
        return new PinpointGetTransactionDetailResponse(request.traceId(), transactionInfo);
    }

    @SuppressWarnings("unchecked")
    public PinpointGetAgentStatResponse getAgentStat(PinpointGetAgentStatRequest request) {
        String path = String.format(
            AGENT_STAT_CHART_PATH + "?agentId=%s&from=%d&to=%d",
            request.chartType(),
            encode(request.agentId()),
            request.from(),
            request.to()
        );
        JsonNode response = apiClient.get(path);

        Map<String, Object> chartData = objectMapper.convertValue(response, Map.class);
        return new PinpointGetAgentStatResponse(request.agentId(), request.chartType(), chartData);
    }

    @SuppressWarnings("unchecked")
    public PinpointGetTransactionTimelineResponse getTransactionTimeline(PinpointGetTransactionTimelineRequest request) {
        long focusTimestamp = request.focusTimestamp() != null ? request.focusTimestamp() : 0L;
        String path = String.format(
            "%s?traceId=%s&focusTimestamp=%d",
            TRANSACTION_TIMELINE_PATH,
            encode(request.traceId()),
            focusTimestamp
        );
        JsonNode response = apiClient.get(path);

        Map<String, Object> timelineData = objectMapper.convertValue(response, Map.class);
        return new PinpointGetTransactionTimelineResponse(request.traceId(), timelineData);
    }

    @SuppressWarnings("unchecked")
    public PinpointGetDetailedAgentInfoResponse getDetailedAgentInfo(PinpointGetDetailedAgentInfoRequest request) {
        String path = String.format(
            "%s?agentId=%s&timestamp=%d",
            DETAILED_AGENT_INFO_PATH,
            encode(request.agentId()),
            request.timestamp()
        );
        JsonNode response = apiClient.get(path);

        Map<String, Object> agentInfo = objectMapper.convertValue(response, Map.class);
        return new PinpointGetDetailedAgentInfoResponse(request.agentId(), agentInfo);
    }

    @SuppressWarnings("unchecked")
    public PinpointGetResponseHistogramResponse getResponseHistogram(PinpointGetResponseHistogramRequest request) {
        String path = String.format(
            "%s?applicationName=%s&serviceTypeName=%s&from=%d&to=%d",
            RESPONSE_HISTOGRAM_PATH,
            encode(request.applicationName()),
            encode(request.serviceTypeName()),
            request.from(),
            request.to()
        );
        JsonNode response = apiClient.get(path);

        Map<String, Object> histogramData = objectMapper.convertValue(response, Map.class);
        return new PinpointGetResponseHistogramResponse(request.applicationName(), histogramData);
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
