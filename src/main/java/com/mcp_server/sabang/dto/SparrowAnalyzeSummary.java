package com.mcp_server.sabang.dto;

import java.util.Map;

public record SparrowAnalyzeSummary(
        int totalAlarms,
        int lineReviewCandidateCount,
        Map<String, Integer> alarmsByRule,
        Map<String, Integer> alarmsByFile
) {

}
