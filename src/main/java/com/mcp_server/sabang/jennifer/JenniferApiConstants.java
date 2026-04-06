package com.mcp_server.sabang.jennifer;

public final class JenniferApiConstants {

    private JenniferApiConstants() {
    }

    public static final String DOMAIN_PATH = "/api/domain";
    public static final String INSTANCE_PATH = "/api/instance";
    public static final String REALTIME_DOMAIN_PATH = "/api/realtime/domain";
    public static final String REALTIME_INSTANCE_PATH = "/api/realtime/instance";
    public static final String ACTIVE_SERVICE_LIST_PATH = "/api/activeService/list";
    public static final String TRANSACTION_TIME_PATH = "/api/transaction/time";
    public static final String TRANSACTION_TXID_PATH = "/api/transaction/txid";
    public static final String TRANSACTION_PROFILE_PATH = "/api/transaction/profile.txt";
    public static final String ERROR_SEARCH_PATH = "/api/dbsearch/error";
    public static final String INSTANCE_METRICS_PATH = "/api/dbmetrics/instance";

    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE_JSON = "application/json";
}
