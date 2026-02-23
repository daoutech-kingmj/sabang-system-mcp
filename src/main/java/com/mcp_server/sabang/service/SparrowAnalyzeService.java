package com.mcp_server.sabang.service;

import com.mcp_server.sabang.dto.SparrowAnalyzeRequest;
import com.mcp_server.sabang.dto.SparrowAnalyzeReport;
import com.mcp_server.sabang.dto.SparrowAnalyzeIssue;
import com.mcp_server.sabang.dto.SparrowAnalyzeResponse;
import com.mcp_server.sabang.dto.SparrowAnalyzeSummary;
import com.mcp_server.sabang.dto.SparrowAnalyzeJobStatusResponse;
import com.mcp_server.sabang.dto.SparrowAnalyzeJobSubmitResponse;
import com.mcp_server.sabang.exception.SparrowExecutionException;
import com.mcp_server.sabang.model.SparrowJobState;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.annotation.PreDestroy;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * SPARROW 정적 분석 도구를 실행하는 서비스
 */
@Service
public class SparrowAnalyzeService {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<String, SparrowJobState> jobs = new ConcurrentHashMap<>();

    public SparrowAnalyzeJobSubmitResponse submitAnalyze(SparrowAnalyzeRequest request) {
        validateRequest(request);

        String jobId = UUID.randomUUID().toString();
        SparrowJobState state = SparrowJobState.pending(jobId, request.projectId());
        jobs.put(jobId, state);

        executor.submit(() -> runAnalyzeJob(state, request));

        return new SparrowAnalyzeJobSubmitResponse(jobId, request.projectId(), state.status());
    }

    public SparrowAnalyzeJobStatusResponse getJobStatus(String jobId, int waitSeconds) {
        SparrowJobState state = jobs.get(jobId);
        if (state == null) {
            throw new SparrowExecutionException("Job not found: " + jobId);
        }
        int boundedWaitSeconds = Math.max(1, Math.min(waitSeconds, 30));
        try {
            state.awaitTerminal(boundedWaitSeconds * 1000L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return state.toResponse();
    }

    /**
     * SPARROW 클라이언트를 실행하여 정적 분석을 수행합니다.
     *
     * @param request SPARROW 분석 요청 (서버 URL, 프로젝트 ID, 변경 파일 등)
     * @return SPARROW 분석 결과
     */
    public SparrowAnalyzeResponse analyze(SparrowAnalyzeRequest request) {
        validateRequest(request);

        List<String> command = buildCommand(request);
        ProcessBuilder processBuilder = createProcessBuilder(command, request);

        try {
            Process process = processBuilder.start();
            
            String output = readStream(process.getInputStream());
            String error = readStream(process.getErrorStream());

            int exitCode = process.waitFor();
            SparrowAnalyzeReport report = parseReport(processBuilder.directory().toPath());

            return new SparrowAnalyzeResponse(request.projectId(), exitCode, output, error, report);
        } catch (IOException ex) {
            throw new SparrowExecutionException("Failed to execute SPARROW client", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SparrowExecutionException("SPARROW execution interrupted", ex);
        }
    }

    private void runAnalyzeJob(SparrowJobState state, SparrowAnalyzeRequest request) {
        state.markRunning();
        try {
            SparrowAnalyzeResponse response = analyze(request);
            if (response.exitCode() == 0) {
                state.markSucceeded(response);
            } else {
                state.markFailed(response, "SPARROW exited with code " + response.exitCode());
            }
        } catch (RuntimeException ex) {
            state.markFailed(ex);
        }
    }

    private void validateRequest(SparrowAnalyzeRequest request) {
        if (request.clientPath() == null || request.clientPath().isBlank()) {
            throw new SparrowExecutionException("Client path is required");
        }
        if (request.serverUrl() == null || request.serverUrl().isBlank()) {
            throw new SparrowExecutionException("Server URL is required");
        }
        if (request.projectId() == null || request.projectId().isBlank()) {
            throw new SparrowExecutionException("Project ID is required");
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new SparrowExecutionException("Username is required");
        }
        if (request.passwordPath() == null || request.passwordPath().isBlank()) {
            throw new SparrowExecutionException("Password path is required");
        }
        if (request.changedFiles() == null || request.changedFiles().isBlank()) {
            throw new SparrowExecutionException("Changed files list is required");
        }
    }

    private List<String> buildCommand(SparrowAnalyzeRequest request) {
        List<String> command = new ArrayList<>();
        command.add(request.clientPath());
        command.add("-P");
        command.add(request.projectId());
        command.add("-U");
        command.add(request.username());
        command.add("-S");
        command.add(request.serverUrl());
        command.add("-PW");
        command.add(request.passwordPath());
        command.add("-SD");
        command.add(request.changedFiles());
        
        return command;
    }

    private ProcessBuilder createProcessBuilder(List<String> command, SparrowAnalyzeRequest request) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        File workingDirectory = resolveWorkingDirectory(request.clientPath());
        processBuilder.directory(workingDirectory);
        processBuilder.redirectErrorStream(false);
        return processBuilder;
    }

    private File resolveWorkingDirectory(String clientPath) {
        Path parent = Path.of(clientPath).getParent();
        if (parent == null) {
            throw new SparrowExecutionException("Client path must include a parent directory");
        }
        return parent.toFile();
    }

    private String readStream(java.io.InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
        }
        return result.toString();
    }

    private SparrowAnalyzeReport parseReport(Path workingDirectory) {
        Path reportPath = workingDirectory.resolve("sparrow").resolve("xml_files").resolve("FINCH_SYN.1.0.xml");
        if (!Files.exists(reportPath)) {
            return new SparrowAnalyzeReport(reportPath.toString(), emptySummary(), Collections.emptyList());
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(reportPath.toFile());
            NodeList alarms = document.getElementsByTagName("alarm");

            List<SparrowAnalyzeIssue> issues = new ArrayList<>();
            Map<String, Integer> alarmsByRule = new LinkedHashMap<>();
            Map<String, Integer> alarmsByFile = new LinkedHashMap<>();
            int lineReviewCandidates = 0;

            for (int i = 0; i < alarms.getLength(); i++) {
                Element alarm = (Element) alarms.item(i);
                Element defect = getFirstChildElement(alarm, "defect");
                Element loc = defect == null ? null : getFirstChildElement(defect, "loc");
                Element event = getDefectEvent(alarm);
                Element desc = event == null ? null : getFirstChildElement(event, "desc");

                String localId = alarm.getAttribute("localId");
                String rule = getChildText(defect, "rule");
                String file = getChildText(loc, "file");
                Integer line = parseInteger(getChildText(loc, "line"));
                String function = getChildText(loc, "func");
                String className = getChildText(loc, "class");
                String tag = getChildText(event, "tag");
                String descriptionId = desc == null ? "" : trim(desc.getAttribute("id"));
                boolean lineReviewRecommended = file != null && !file.isBlank() && line != null && line > 0;
                String lineReviewReason = lineReviewRecommended
                        ? "Potential defect location from SPARROW alarm"
                        : "Line information is missing in SPARROW alarm";

                if (lineReviewRecommended) {
                    lineReviewCandidates++;
                }
                incrementCount(alarmsByRule, emptyToUnknown(rule));
                incrementCount(alarmsByFile, emptyToUnknown(file));

                issues.add(new SparrowAnalyzeIssue(
                        localId,
                        rule,
                        file,
                        line,
                        function,
                        className,
                        tag,
                        descriptionId,
                        lineReviewRecommended,
                        lineReviewReason
                ));
            }

            SparrowAnalyzeSummary summary = new SparrowAnalyzeSummary(
                    alarms.getLength(),
                    lineReviewCandidates,
                    alarmsByRule,
                    alarmsByFile
            );
            return new SparrowAnalyzeReport(reportPath.toString(), summary, issues);
        } catch (Exception ex) {
            throw new SparrowExecutionException("Failed to parse SPARROW XML report: " + reportPath, ex);
        }
    }

    private SparrowAnalyzeSummary emptySummary() {
        return new SparrowAnalyzeSummary(0, 0, Collections.emptyMap(), Collections.emptyMap());
    }

    private void incrementCount(Map<String, Integer> counts, String key) {
        counts.merge(key, 1, Integer::sum);
    }

    private String emptyToUnknown(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value;
    }

    private Element getDefectEvent(Element alarm) {
        NodeList events = alarm.getElementsByTagName("event");
        for (int i = 0; i < events.getLength(); i++) {
            Element event = (Element) events.item(i);
            String defectAttr = event.getAttribute("defect");
            if ("true".equalsIgnoreCase(defectAttr)) {
                return event;
            }
        }
        return null;
    }

    private Element getFirstChildElement(Element parent, String tagName) {
        if (parent == null) {
            return null;
        }
        NodeList children = parent.getElementsByTagName(tagName);
        if (children.getLength() == 0) {
            return null;
        }
        return (Element) children.item(0);
    }

    private String getChildText(Element parent, String tagName) {
        Element child = getFirstChildElement(parent, tagName);
        if (child == null) {
            return "";
        }
        return trim(child.getTextContent());
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

}
