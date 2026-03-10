package com.mcp_server.sabang.opensearch;

public final class OpenSearchApiConstants {

    private OpenSearchApiConstants() {
    }

    public static final String SQL_PATH = "/_plugins/_sql";
    public static final String CAT_INDICES_PATH = "/_cat/indices?format=json&h=index,health,status,docs.count,store.size";
    public static final String MAPPING_PATH = "/%s/_mapping";

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String CONTENT_TYPE_JSON = "application/json";
}