# sabang-system-mcp

사내 코드리뷰/정적분석 자동화를 위해 만든 **Spring AI 기반 MCP 서버**입니다.  
`stdio` 모드로 동작하며, LLM 에이전트가 Bitbucket PR/GitLab MR 조회 및 코멘트 작성, SPARROW 분석 실행을 직접 호출할 수 있습니다.

## 핵심 기능

- Bitbucket PR 조회 (`bitbucket-pr-get`)
- Bitbucket PR 일반 코멘트 작성 (`bitbucket-pr-comment-post`)
- Bitbucket PR 라인 코멘트 작성 (`bitbucket-pr-line-comment-post`)
- GitLab MR 조회 (`gitlab-mr-get`)
- GitLab MR 일반 코멘트 작성 (`gitlab-mr-comment-post`)
- GitLab MR 라인 코멘트 작성 (`gitlab-mr-line-comment-post`)
- SPARROW 분석 Job 제출 (`sparrow-analyze`)
- SPARROW 분석 Job 상태 조회 (`sparrow-analyze-status`)

## 기술 스택

- Java 21
- Spring Boot 4.0.2
- Spring AI MCP Server Starter (`org.springframework.ai:spring-ai-starter-mcp-server`)
- Gradle

## 프로젝트 구조

```text
src/main/java/com/mcp_server/sabang
├── tool/          # MCP 툴 엔트리포인트 (@McpTool)
├── service/       # 비즈니스 로직
├── bitbucket/     # Bitbucket URL 파싱/API 클라이언트/검증
├── gitlab/        # GitLab URL 파싱/API 클라이언트/검증
├── dto/           # 요청/응답 스키마
├── model/         # SPARROW Job 상태 모델
└── exception/     # 도메인 예외
```

## 동작 방식

- 서버는 `application.yaml`에서 `spring.ai.mcp.server.stdio: true`로 설정되어 있습니다.
- 즉, HTTP 서버가 아니라 **MCP 클라이언트 프로세스가 stdio로 직접 붙는 형태**입니다.
- GitLab/Bitbucket API는 Java `HttpClient`로 직접 호출합니다.
- SPARROW 분석은 외부 클라이언트(사용자) 스크립트를 별도 프로세스로 실행하며, Job 상태를 메모리에서 관리합니다.

## 빌드 및 실행

### 1) 로컬 빌드

```bash
./gradlew clean build
```

### 2) MCP 서버 실행

```bash
java -jar build/libs/sabang-0.0.1-SNAPSHOT.jar
```

## MCP 클라이언트 연결 예시

아래는 `stdio` 기반 MCP 서버 등록 예시입니다(클라이언트별 키 이름은 다를 수 있음).

```json
{
  "mcpServers": {
    "sabang": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/sabang-system-mcp/build/libs/sabang-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

## 툴 스펙 요약

### Bitbucket

1. `bitbucket-pr-get`
- 입력: `prUrl`, `pat`
- 출력: PR 메타정보 + 변경 파일 목록 + 커밋 목록

2. `bitbucket-pr-comment-post`
- 입력: `prUrl`, `pat`, `body`
- 출력: 생성된 `commentId`, `webUrl`

3. `bitbucket-pr-line-comment-post`
- 입력: `prUrl`, `pat`, `body`, `path`, `line`, `lineType`
- `lineType`: `ADDED | REMOVED | CONTEXT`

### GitLab

1. `gitlab-mr-get`
- 입력: `mrUrl`, `pat`
- 출력: MR 메타정보 + 변경 파일 목록 + 버전(sha) 목록

2. `gitlab-mr-comment-post`
- 입력: `mrUrl`, `pat`, `body`
- 출력: 생성된 `noteId`, `webUrl`

3. `gitlab-mr-line-comment-post`
- 입력: `mrUrl`, `pat`, `body`, `path`, `line`, `lineType`
- `lineType`: `ADDED | REMOVED`

### SPARROW

1. `sparrow-analyze`
- 입력:
  - `serverUrl`
  - `clientPath`
  - `passwordPath`
  - `projectId`
  - `username`
  - `changedFiles` (절대경로 `:` 구분)
- 출력: `jobId`, `projectId`, `status(PENDING)`

2. `sparrow-analyze-status`
- 입력: `jobId`, `waitSeconds(1~30 권장)`
- 출력: 상태(`PENDING/RUNNING/SUCCEEDED/FAILED`), `exitCode`, `output`, `error`, 타임스탬프

## URL 형식 규칙

- Bitbucket PR URL 예시  
  `https://bitbucket.example.com/projects/PROJ/repos/my-repo/pull-requests/123`
- GitLab MR URL 예시  
  `https://gitlab.example.com/group/project/-/merge_requests/123`

URL 파싱에 실패하면 `InvalidPrUrlException`, `InvalidMrUrlException`이 발생합니다.

## 운영/보안 주의사항

- PAT는 툴 입력으로 전달되며, **최소 권한 토큰**을 사용해야 합니다.
- SPARROW 실행 경로(`clientPath`, `passwordPath`)는 서버 프로세스 권한으로 접근 가능해야 합니다.
- SPARROW Job 상태는 현재 메모리 기반이라 재시작 시 유실됩니다.

## 테스트

```bash
./gradlew test
```
