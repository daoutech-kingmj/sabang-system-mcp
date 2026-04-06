package com.mcp_server.sabang.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp_server.sabang.dto.JenniferGetActiveServicesRequest;
import com.mcp_server.sabang.dto.JenniferGetActiveServicesResponse;
import com.mcp_server.sabang.dto.JenniferGetInstanceMetricsRequest;
import com.mcp_server.sabang.dto.JenniferGetInstanceMetricsResponse;
import com.mcp_server.sabang.dto.JenniferGetInstancesRequest;
import com.mcp_server.sabang.dto.JenniferGetInstancesResponse;
import com.mcp_server.sabang.dto.JenniferGetInstancesResponse.InstanceInfo;
import com.mcp_server.sabang.dto.JenniferGetRealtimeDomainRequest;
import com.mcp_server.sabang.dto.JenniferGetRealtimeDomainResponse;
import com.mcp_server.sabang.dto.JenniferGetRealtimeDomainResponse.RealtimeDomainData;
import com.mcp_server.sabang.dto.JenniferGetRealtimeInstanceRequest;
import com.mcp_server.sabang.dto.JenniferGetRealtimeInstanceResponse;
import com.mcp_server.sabang.dto.JenniferGetRealtimeInstanceResponse.RealtimeInstanceData;
import com.mcp_server.sabang.dto.JenniferGetTransactionDetailRequest;
import com.mcp_server.sabang.dto.JenniferGetTransactionDetailResponse;
import com.mcp_server.sabang.dto.JenniferGetTransactionDetailResponse.TransactionDetail;
import com.mcp_server.sabang.dto.JenniferGetTransactionProfileRequest;
import com.mcp_server.sabang.dto.JenniferGetTransactionProfileResponse;
import com.mcp_server.sabang.dto.JenniferGetTransactionsRequest;
import com.mcp_server.sabang.dto.JenniferGetTransactionsResponse;
import com.mcp_server.sabang.dto.JenniferGetTransactionsResponse.TransactionSummary;
import com.mcp_server.sabang.dto.JenniferListDomainsResponse;
import com.mcp_server.sabang.dto.JenniferListDomainsResponse.DomainInfo;
import com.mcp_server.sabang.dto.JenniferSearchErrorsRequest;
import com.mcp_server.sabang.dto.JenniferSearchErrorsResponse;
import com.mcp_server.sabang.dto.JenniferSearchErrorsResponse.ErrorInfo;
import com.mcp_server.sabang.jennifer.JenniferApiClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

import static com.mcp_server.sabang.jennifer.JenniferApiConstants.*;

@Service
public class JenniferService {

    private final JenniferApiClient apiClient;
    private final ObjectMapper objectMapper;

    public JenniferService(JenniferApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    public JenniferListDomainsResponse listDomains() {
        JsonNode response = apiClient.get(DOMAIN_PATH);
        JsonNode result = response.path("result");

        List<DomainInfo> domains = new ArrayList<>();
        if (result.isArray()) {
            for (JsonNode node : result) {
                List<String> hierarchy = new ArrayList<>();
                JsonNode groupNode = node.path("groupHierarchy");
                if (groupNode.isArray()) {
                    for (JsonNode g : groupNode) {
                        hierarchy.add(g.asText(""));
                    }
                }
                JsonNode ic = node.path("instanceCount");
                domains.add(new DomainInfo(
                    node.path("domainId").asInt(0),
                    node.path("name").asText(""),
                    node.path("description").asText(""),
                    hierarchy,
                    ic.path("total").asInt(0),
                    ic.path("live").asInt(0),
                    ic.path("stopped").asInt(0)
                ));
            }
        }

        return new JenniferListDomainsResponse(domains);
    }

    public JenniferGetInstancesResponse getInstances(JenniferGetInstancesRequest request) {
        String path = INSTANCE_PATH + "?domain_id=" + request.domainId();
        JsonNode response = apiClient.get(path);
        JsonNode result = response.path("result");

        List<InstanceInfo> instances = new ArrayList<>();
        if (result.isArray()) {
            for (JsonNode node : result) {
                instances.add(new InstanceInfo(
                    node.path("instanceId").asInt(0),
                    node.path("name").asText(""),
                    node.path("version").asText(""),
                    node.path("ipAddress").asText(""),
                    node.path("hostName").asText(""),
                    node.path("platform").asText(""),
                    node.path("status").asText("")
                ));
            }
        }

        return new JenniferGetInstancesResponse(request.domainId(), instances);
    }

    public JenniferGetRealtimeDomainResponse getRealtimeDomain(JenniferGetRealtimeDomainRequest request) {
        int domainId = request.domainId() != null ? request.domainId() : 0;
        String path = REALTIME_DOMAIN_PATH + "?domain_id=" + domainId;
        JsonNode response = apiClient.get(path);
        JsonNode result = response.path("result");

        List<RealtimeDomainData> domains = new ArrayList<>();
        if (result.isArray()) {
            for (JsonNode node : result) {
                domains.add(new RealtimeDomainData(
                    node.path("domainId").asInt(0),
                    node.path("domainName").asText(""),
                    node.path("activeService").asInt(0),
                    node.path("activeUser").asInt(0),
                    node.path("tps").asDouble(0.0),
                    node.path("responseTime").asDouble(0.0),
                    node.path("concurrentUser").asDouble(0.0),
                    node.path("rejectRate").asDouble(0.0),
                    node.path("hitHour").asLong(0),
                    node.path("hitDay").asLong(0),
                    node.path("visitHour").asLong(0),
                    node.path("visitDay").asLong(0)
                ));
            }
        }

        return new JenniferGetRealtimeDomainResponse(domains);
    }

    @SuppressWarnings("unchecked")
    public JenniferGetRealtimeInstanceResponse getRealtimeInstance(JenniferGetRealtimeInstanceRequest request) {
        int instanceId = request.instanceId() != null ? request.instanceId() : 0;
        String path = REALTIME_INSTANCE_PATH + "?domain_id=" + request.domainId() + "&instance_id=" + instanceId;
        JsonNode response = apiClient.get(path);
        JsonNode result = response.path("result");

        List<RealtimeInstanceData> instances = new ArrayList<>();
        if (result.isArray()) {
            for (JsonNode node : result) {
                Map<String, Double> serviceRateByRange = new HashMap<>();
                JsonNode rangeNode = node.path("serviceRateByRange");
                if (rangeNode.isObject()) {
                    var fields = rangeNode.fields();
                    while (fields.hasNext()) {
                        var entry = fields.next();
                        serviceRateByRange.put(entry.getKey(), entry.getValue().asDouble(0.0));
                    }
                }
                instances.add(new RealtimeInstanceData(
                    node.path("instanceId").asInt(0),
                    node.path("instanceName").asText(""),
                    node.path("activeService").asInt(0),
                    node.path("tps").asDouble(0.0),
                    node.path("responseTime").asDouble(0.0),
                    node.path("concurrentUser").asDouble(0.0),
                    node.path("procCPU").asDouble(0.0),
                    node.path("procMemory").asDouble(0.0),
                    node.path("heapCommitted").asDouble(0.0),
                    node.path("heapUsed").asDouble(0.0),
                    serviceRateByRange,
                    node.path("hitHour").asLong(0),
                    node.path("hitDay").asLong(0)
                ));
            }
        }

        return new JenniferGetRealtimeInstanceResponse(request.domainId(), instances);
    }

    @SuppressWarnings("unchecked")
    public JenniferGetActiveServicesResponse getActiveServices(JenniferGetActiveServicesRequest request) {
        String path = ACTIVE_SERVICE_LIST_PATH + "?domain_id=" + request.domainId();
        if (request.instanceId() != null && !request.instanceId().isBlank()) {
            path += "&instance_id=" + encode(request.instanceId());
        }
        JsonNode response = apiClient.get(path);
        JsonNode result = response.path("result");

        List<Map<String, Object>> services = new ArrayList<>();
        if (result.isArray()) {
            for (JsonNode node : result) {
                services.add(objectMapper.convertValue(node, Map.class));
            }
        }

        return new JenniferGetActiveServicesResponse(request.domainId(), services.size(), services);
    }

    public JenniferGetTransactionsResponse getTransactions(JenniferGetTransactionsRequest request) {
        String path = TRANSACTION_TIME_PATH + "?domain_id=" + request.domainId()
            + "&start_time=" + request.startTime()
            + "&end_time=" + request.endTime();
        if (request.instanceId() != null) {
            path += "&instance_id=" + request.instanceId();
        }
        JsonNode response = apiClient.get(path);
        JsonNode result = response.path("result");

        List<TransactionSummary> transactions = new ArrayList<>();
        if (result.isArray()) {
            for (JsonNode node : result) {
                transactions.add(new TransactionSummary(
                    node.path("txid").asText(""),
                    node.path("instanceName").asText(""),
                    node.path("applicationName").asText(""),
                    node.path("clientIp").asText(""),
                    parseLong(node.path("startTime")),
                    parseLong(node.path("endTime")),
                    node.path("responseTime").asLong(0),
                    node.path("cpuTime").asLong(0),
                    node.path("sqlTime").asLong(0),
                    node.path("externalcallTime").asLong(0),
                    node.path("errorType").asText("")
                ));
            }
        }

        return new JenniferGetTransactionsResponse(request.domainId(), transactions.size(), transactions);
    }

    public JenniferGetTransactionDetailResponse getTransactionDetail(JenniferGetTransactionDetailRequest request) {
        String path = TRANSACTION_TXID_PATH + "?domain_id=" + request.domainId()
            + "&txid=" + encode(request.txid())
            + "&time=" + request.time();
        JsonNode response = apiClient.get(path);
        JsonNode result = response.path("result");

        List<TransactionDetail> details = new ArrayList<>();
        if (result.isArray()) {
            for (JsonNode node : result) {
                details.add(new TransactionDetail(
                    node.path("instanceName").asText(""),
                    node.path("applicationName").asText(""),
                    node.path("clientIp").asText(""),
                    node.path("responseTime").asLong(0),
                    node.path("cpuTime").asLong(0),
                    node.path("sqlTime").asLong(0),
                    node.path("externalcallTime").asLong(0),
                    node.path("errorType").asText("")
                ));
            }
        }

        return new JenniferGetTransactionDetailResponse(request.txid(), details);
    }

    public JenniferGetTransactionProfileResponse getTransactionProfile(JenniferGetTransactionProfileRequest request) {
        String path = TRANSACTION_PROFILE_PATH + "?domain_id=" + request.domainId()
            + "&txid=" + encode(request.txid())
            + "&time=" + request.time();
        String profile = apiClient.getText(path);
        return new JenniferGetTransactionProfileResponse(request.txid(), profile);
    }

    public JenniferSearchErrorsResponse searchErrors(JenniferSearchErrorsRequest request) {
        String path = ERROR_SEARCH_PATH + "?domain_id=" + request.domainId()
            + "&start_time=" + request.startTime()
            + "&end_time=" + request.endTime();
        if (request.instanceId() != null && !request.instanceId().isBlank()) {
            path += "&instance_id=" + encode(request.instanceId());
        }
        if (request.errorType() != null && !request.errorType().isBlank()) {
            path += "&error_type=" + encode(request.errorType());
        }
        JsonNode response = apiClient.get(path);
        JsonNode result = response.path("result");

        List<ErrorInfo> errors = new ArrayList<>();
        if (result.isArray()) {
            for (JsonNode node : result) {
                errors.add(new ErrorInfo(
                    node.path("instanceName").asText(""),
                    node.path("errorType").asText(""),
                    node.path("message").asText(""),
                    node.path("applicationName").asText(""),
                    node.path("txid").asText(""),
                    node.path("time").asText("")
                ));
            }
        }

        return new JenniferSearchErrorsResponse(request.domainId(), errors.size(), errors);
    }

    @SuppressWarnings("unchecked")
    public JenniferGetInstanceMetricsResponse getInstanceMetrics(JenniferGetInstanceMetricsRequest request) {
        String path = INSTANCE_METRICS_PATH + "?domain_id=" + request.domainId()
            + "&instance_id=" + request.instanceId()
            + "&start_time=" + request.startTime()
            + "&end_time=" + request.endTime()
            + "&interval_minute=" + request.intervalMinute()
            + "&metrics=" + request.metrics();
        JsonNode response = apiClient.get(path);

        Map<String, Object> metricsData = objectMapper.convertValue(response, Map.class);
        return new JenniferGetInstanceMetricsResponse(request.domainId(), request.instanceId(), metricsData);
    }

    private static long parseLong(JsonNode node) {
        if (node.isNumber()) {
            return node.asLong(0);
        }
        try {
            return Long.parseLong(node.asText("0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
