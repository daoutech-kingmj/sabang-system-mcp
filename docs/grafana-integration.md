# Grafana Integration

Grafana Loki 로그 조회를 위한 MCP 도구 구현 설명서.

---

## 파일 구조

```
grafana/
  GrafanaApiClient.java         # HTTP 클라이언트 (인증 + 요청 전송)
  GrafanaApiConstants.java      # API 경로 상수

exception/
  GrafanaApiException.java      # 도메인 예외

dto/
  GrafanaListDatasourcesRequest.java
  GrafanaListDatasourcesResponse.java
  GrafanaLokiLabelsRequest.java
  GrafanaLokiLabelsResponse.java
  GrafanaLokiQueryRequest.java
  GrafanaLokiQueryResponse.java

service/
  GrafanaService.java           # 비즈니스 로직

tool/
  GrafanaTools.java             # MCP 도구 노출
```

---

## 노출된 MCP 도구 (3개)

### 1. `grafana-list-datasources`
모든 데이터소스 목록을 반환한다. 파라미터 없음.
**목적:** Loki 데이터소스의 숫자 `id`를 얻기 위해 먼저 호출한다. 이후 모든 Loki 관련 도구에서 이 `id`를 사용한다.

**응답 예시:**
```json
{
  "datasources": [
    { "id": 3, "uid": "abc123", "name": "Loki-Prod", "type": "loki" },
    { "id": 1, "uid": "xyz456", "name": "Prometheus", "type": "prometheus" }
  ]
}
```

---

### 2. `grafana-loki-labels`
`label` 파라미터 유무에 따라 동작이 달라진다.

| `label` 값 | 호출 API | 반환 내용 |
|---|---|---|
| 비어있음 | `GET /loki/api/v1/labels` | 레이블 이름 목록 (`app`, `namespace`, `level` 등) |
| 값 있음 (e.g. `app`) | `GET /loki/api/v1/label/app/values` | 해당 레이블의 값 목록 (`api-server`, `worker` 등) |

**목적:** LLM이 유효한 LogQL 쿼리를 구성하기 위해 사용 가능한 레이블과 값을 먼저 파악한다.

**응답 예시 (label 없음):**
```json
{ "label": "__labels__", "values": ["app", "namespace", "level", "host"] }
```

**응답 예시 (label = "app"):**
```json
{ "label": "app", "values": ["api-server", "batch-worker", "gateway"] }
```

---

### 3. `grafana-loki-query`
LogQL 쿼리를 실행하고 매칭된 로그 라인을 반환한다.

**파라미터:**

| 파라미터 | 필수 | 설명 |
|---|---|---|
| `datasourceId` | ✅ | 숫자형 데이터소스 ID |
| `query` | ✅ | LogQL 쿼리 문자열 |
| `from` | ❌ | 시작 시간 (기본값: `now-1h`) |
| `to` | ❌ | 종료 시간 (기본값: `now`) |
| `limit` | ❌ | 최대 로그 라인 수, 1~1000 (기본값: 100) |

**응답 구조:**
```json
{
  "query": "{app=\"api-server\"} |= \"ERROR\"",
  "from": "2024-01-01 09:00:00.000",
  "to": "2024-01-01 10:00:00.000",
  "totalLines": 3,
  "streams": [
    {
      "labels": { "app": "api-server", "namespace": "prod" },
      "lines": [
        { "timestamp": "2024-01-01 09:55:12.345", "message": "ERROR: NullPointerException at ..." },
        { "timestamp": "2024-01-01 09:42:08.100", "message": "ERROR: Connection timeout" }
      ]
    }
  ]
}
```

`streams`는 Loki의 스트림 단위(레이블 조합)로 그룹핑된다. 같은 `{app, namespace}` 조합이면 하나의 스트림에 묶인다.

---

## 시간 파라미터 (`from` / `to`) 상세

`GrafanaService.parseTime()` 에서 처리하며 두 가지 포맷을 지원한다.

### 상대 시간 (`now-{숫자}{단위}`)

| 예시 | 의미 |
|---|---|
| `now-30m` | 30분 전 |
| `now-1h` | 1시간 전 |
| `now-24h` | 24시간 전 |
| `now-7d` | 7일 전 |

지원 단위: `s`(초), `m`(분), `h`(시간), `d`(일)

### 절대 시간 (ISO-8601)

```
2024-01-01T00:00:00Z
2024-01-01T09:00:00+09:00
```

### Loki API로의 변환

Loki는 시간을 **나노초 Unix timestamp**로 받는다. 변환 로직:

```java
long nanos = instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
```

응답의 `timestamp` 필드는 서버 로컬 시간대(`ZoneId.systemDefault()`)로 포맷팅된 사람이 읽기 쉬운 형태로 변환된다.

---

## 인증

Basic Auth를 사용한다. `application.yaml`에 설정된 username/password를 Base64로 인코딩해 모든 요청 헤더에 포함한다.

```
Authorization: Basic base64(username:password)
```

설정 위치 (`application.yaml`):
```yaml
grafana:
  host: ${GRAFANA_HOST:}
  username: ${GRAFANA_USERNAME:}
  password: ${GRAFANA_PASSWORD:}
```

환경변수 또는 Spring 프로파일 파일(`application-dev.yaml` 등)에 실제 값을 설정한다.
프로파일 파일은 `.gitignore`에 의해 커밋되지 않는다.

---

## 호출되는 Grafana API 경로

| 도구 | Method | 경로 |
|---|---|---|
| `grafana-list-datasources` | GET | `/api/datasources` |
| `grafana-loki-labels` (레이블명) | GET | `/api/datasources/proxy/{id}/loki/api/v1/labels` |
| `grafana-loki-labels` (레이블값) | GET | `/api/datasources/proxy/{id}/loki/api/v1/label/{label}/values` |
| `grafana-loki-query` | GET | `/api/datasources/proxy/{id}/loki/api/v1/query_range` |

`/api/datasources/proxy/{id}/...` 경로는 Grafana가 프록시 역할을 해서 Loki로 요청을 중계한다. 클라이언트는 Loki 주소를 직접 알 필요가 없다.

---

## 전형적인 사용 흐름

```
1. grafana-list-datasources
   → Loki 데이터소스 id 확인 (예: id=3)

2. grafana-loki-labels { datasourceId: 3 }
   → 레이블 목록 확인: ["app", "namespace", "level"]

3. grafana-loki-labels { datasourceId: 3, label: "app" }
   → app 값 확인: ["api-server", "batch-worker"]

4. grafana-loki-query {
     datasourceId: 3,
     query: "{app=\"api-server\"} |= \"ERROR\"",
     from: "now-1h",
     limit: 50
   }
   → 에러 로그 조회
```

---

## LogQL 쿼리 예시

```logql
# 특정 앱의 ERROR 로그
{app="api-server"} |= "ERROR"

# JSON 파싱 후 level 필터
{namespace="prod"} | json | level="error"

# 스택트레이스 포함 로그
{app="api-server"} |= "Exception"

# 특정 사용자 관련 로그
{app="api-server"} |= "userId=12345"

# 여러 앱에서 에러 찾기
{namespace="prod"} |= "ERROR" | json | app=~"api-server|gateway"
```