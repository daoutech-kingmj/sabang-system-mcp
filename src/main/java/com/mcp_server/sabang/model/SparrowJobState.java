package com.mcp_server.sabang.model;

import com.mcp_server.sabang.dto.SparrowAnalyzeJobStatusResponse;
import com.mcp_server.sabang.dto.SparrowAnalyzeResponse;
import java.time.Instant;

public final class SparrowJobState {
    private final Object monitor = new Object();
    private final String jobId;
    private final String projectId;
    private volatile String status;
    private final Instant createdAt;
    private volatile Instant startedAt;
    private volatile Instant finishedAt;
    private volatile int exitCode;
    private volatile String output;
    private volatile String error;
    private volatile String message;

    private SparrowJobState(String jobId, String projectId) {
        this.jobId = jobId;
        this.projectId = projectId;
        this.status = "PENDING";
        this.createdAt = Instant.now();
        this.startedAt = Instant.EPOCH;
        this.finishedAt = Instant.EPOCH;
        this.exitCode = -1;
        this.output = "";
        this.error = "";
        this.message = "";
    }

    public static SparrowJobState pending(String jobId, String projectId) {
        return new SparrowJobState(jobId, projectId);
    }

    public String status() {
        return status;
    }

    public void markRunning() {
        synchronized (monitor) {
            this.status = "RUNNING";
            this.startedAt = Instant.now();
            monitor.notifyAll();
        }
    }

    public void markSucceeded(SparrowAnalyzeResponse response) {
        synchronized (monitor) {
            this.status = "SUCCEEDED";
            this.exitCode = response.exitCode();
            this.output = response.output();
            this.error = response.error();
            this.finishedAt = Instant.now();
            monitor.notifyAll();
        }
    }

    public void markFailed(SparrowAnalyzeResponse response, String message) {
        synchronized (monitor) {
            this.status = "FAILED";
            this.exitCode = response.exitCode();
            this.output = response.output();
            this.error = response.error();
            this.message = message;
            this.finishedAt = Instant.now();
            monitor.notifyAll();
        }
    }

    public void markFailed(RuntimeException ex) {
        synchronized (monitor) {
            this.status = "FAILED";
            this.message = ex.getMessage();
            this.finishedAt = Instant.now();
            monitor.notifyAll();
        }
    }

    public boolean isTerminal() {
        return "SUCCEEDED".equals(status) || "FAILED".equals(status);
    }

    public void awaitTerminal(long waitMillis) throws InterruptedException {
        long deadline = System.currentTimeMillis() + waitMillis;
        synchronized (monitor) {
            while (!isTerminal()) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    break;
                }
                monitor.wait(remaining);
            }
        }
    }

    public SparrowAnalyzeJobStatusResponse toResponse() {
        return new SparrowAnalyzeJobStatusResponse(
                jobId,
                projectId,
                status,
                createdAt,
                startedAt,
                finishedAt,
                exitCode,
                output,
                error,
                message
        );
    }
}
