package com.mcp_server.sabang.dto;

import java.util.List;

public record JenniferListDomainsResponse(
    List<DomainInfo> domains
) {

    public record DomainInfo(
        int domainId,
        String name,
        String description,
        List<String> groupHierarchy,
        int totalInstances,
        int liveInstances,
        int stoppedInstances
    ) {
    }
}
