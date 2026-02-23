package com.mcp_server.sabang.dto;

public record SparrowAnalyzeIssue(
        String localId,
        String rule,
        String file,
        Integer line,
        String function,
        String className,
        String tag,
        String descriptionId,
        boolean lineReviewRecommended,
        String lineReviewReason
) {

}
