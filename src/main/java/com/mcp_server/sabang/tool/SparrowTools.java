package com.mcp_server.sabang.tool;

import com.mcp_server.sabang.dto.SparrowAnalyzeRequest;
import com.mcp_server.sabang.dto.SparrowAnalyzeJobStatusRequest;
import com.mcp_server.sabang.dto.SparrowAnalyzeJobStatusResponse;
import com.mcp_server.sabang.dto.SparrowAnalyzeJobSubmitResponse;
import com.mcp_server.sabang.service.SparrowAnalyzeService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class SparrowTools {
    private final SparrowAnalyzeService sparrowAnalyzeService;

    public SparrowTools(SparrowAnalyzeService sparrowAnalyzeService) {
        this.sparrowAnalyzeService = sparrowAnalyzeService;
    }

    @McpTool(name="sparrow-analyze", description="Submit SPARROW static analysis job", generateOutputSchema=true)
    public SparrowAnalyzeJobSubmitResponse submitAnalyze(SparrowAnalyzeRequest request) {
        return this.sparrowAnalyzeService.submitAnalyze(request);
    }

    @McpTool(name="sparrow-analyze-status", description="Get SPARROW analysis job status by jobId", generateOutputSchema=true)
    public SparrowAnalyzeJobStatusResponse getAnalyzeStatus(SparrowAnalyzeJobStatusRequest request) {
        return this.sparrowAnalyzeService.getJobStatus(request.jobId(), request.waitSeconds());
    }
}
