package com.mcp_server.sabang.dto;

import java.util.List;

public record SparrowAnalyzeReport(
        String reportPath,
        SparrowAnalyzeSummary summary,
        List<SparrowAnalyzeIssue> details
) {

}
