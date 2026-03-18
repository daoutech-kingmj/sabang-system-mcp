package com.mcp_server.sabang.pinpoint;

public final class PinpointApiConstants {

    private PinpointApiConstants() {
    }

    public static final String APPLICATIONS_PATH = "/applications.pinpoint";
    public static final String AGENT_LIST_PATH = "/getAgentList.pinpoint";
    public static final String AGENT_INFO_PATH = "/getAgentInfo.pinpoint";
    public static final String AGENT_STATUS_PATH = "/getAgentStatus.pinpoint";
    public static final String SERVER_MAP_PATH = "/getServerMapDataV2.pinpoint";
    public static final String SCATTER_DATA_PATH = "/getScatterData.pinpoint";
    public static final String HEATMAP_DRAG_PATH = "/heatmap/drag.pinpoint";
    public static final String TRANSACTION_INFO_PATH = "/transactionInfo.pinpoint";
    public static final String TRANSACTION_TIMELINE_PATH = "/transactionTimelineInfo.pinpoint";
    public static final String AGENT_STAT_CHART_PATH = "/getAgentStat/%s/chart.pinpoint";
    public static final String RESPONSE_HISTOGRAM_PATH = "/getResponseTimeHistogramData.pinpoint";
    public static final String DETAILED_AGENT_INFO_PATH = "/getDetailedAgentInfo.pinpoint";

    public static final String HEADER_ACCEPT = "Accept";
    public static final String CONTENT_TYPE_JSON = "application/json";
}
