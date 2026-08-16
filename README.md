# BATON Backend

시차가 있는 비동기 협업에서 사용자가 미리 승인한 의사결정 범위 안에서만 다음 행동을 실행하는 BATON의 백엔드 서버입니다.

## 처리 흐름

```text
외부 플랫폼 event
→ 메시지 저장 및 중복 검사
→ 원본 메시지에 연결된 BATON 조회
→ LLM structured classification
→ Rule Engine 검증
→ 승인된 Action 실행 또는 사용자 검토 요청
→ 실행 결과와 audit timeline 저장
```

LLM은 자연어를 해석하지만 실행 여부를 결정하지 않습니다. 실제 실행은 서버의 명시적인 규칙과 guardrail을 모두 통과한 경우에만 가능합니다.

## 제품 불변조건

- AI는 새로운 결정, 일정, 비용, 약속 또는 범위를 만들지 않습니다.
- 모든 Branch는 `Condition → Decision → Action`을 저장합니다.
- 하나의 BATON은 최대 한 번만 자동 handoff를 실행합니다.
- 사용자가 직접 개입하면 기존 자동화를 중지합니다.
- BATON-generated message는 다른 BATON의 trigger가 되지 않습니다.
- 애매함, 새 질문, 복수 분기 매칭, 범위 초과, 연결 오류는 자동 실행하지 않습니다.
- 모든 판정과 실행은 추적 가능한 audit record를 남깁니다.

## 기술 스택

초기 구성안이며 프로젝트 초기화 시 버전을 확정합니다.

| 구분 | 기술 |
| --- | --- |
| 언어 | Java |
| 프레임워크 | Spring Boot · Spring Data JPA · Spring AI |
| 데이터베이스 | PostgreSQL |
| 스키마 관리 | Flyway |
| AI | OpenAI API structured output |
| 플랫폼 | Slack Web API · Events API |
| API 문서 | springdoc OpenAPI |
| 빌드 | Gradle |
| 로컬 인프라 | Docker Compose |

## 도메인 모델

```text
User
└─ PlatformConnection
   └─ Conversation
      └─ Message
         └─ Baton
            ├─ Branch
            ├─ Classification
            └─ Execution
```

핵심 테이블은 다음과 같습니다.

- `users`
- `platform_connections`
- `conversations`
- `messages`
- `batons`
- `branches`
- `classifications`
- `executions`

## 권장 패키지 구조

```text
com.likelion.yonsei.baton
├── domain/
│   ├── user
│   ├── platform
│   ├── conversation
│   ├── message
│   ├── baton
│   ├── classification
│   └── execution
├── integration/
│   ├── slack
│   └── openai
├── common/
│   ├── exception
│   ├── response
│   └── time
└── config
```

권장 역할 분리는 다음과 같습니다.

```text
WebhookController
MessageService
BatonService
BranchGenerationService
ClassificationService
RuleEngine
ActionExecutor
PlatformService
AuditLogService
```

## 시작하기

### 사전 요구사항

- 프로젝트에서 확정한 JDK 버전
- Docker Desktop

### 로컬 인프라

```bash
docker compose up -d
docker compose ps
```

### 서버 실행

```bash
cp .env.example .env
./gradlew bootRun
```

### 검증

```bash
./gradlew test
./gradlew build
```

실제 명령과 포트는 프로젝트 초기화 후 Gradle 및 Docker 설정과 함께 확정합니다.

## 환경변수

실제 token과 secret은 저장소에 커밋하지 않습니다. 공개 가능한 변수 목록은 [`.env.example`](.env.example)에 유지합니다.

주요 범주:

- Spring profile 및 server port
- PostgreSQL 연결 정보
- OpenAI API key와 model
- Slack client ID, client secret, signing secret
- token 암호화 key
- Frontend CORS origin

## LLM 출력

분류 결과는 자유 텍스트가 아닌 검증 가능한 structured output으로 받습니다.

```json
{
  "selectedBranchId": 3,
  "confidence": 0.91,
  "ambiguous": false,
  "containsNewQuestion": false,
  "extractedData": {
    "deliveryDate": "2026-03-27"
  },
  "reasoningSummary": "상대방이 3월 27일 제공 가능하다고 명시했습니다."
}
```

`reasoningSummary`는 사용자에게 보여줄 짧은 판정 설명이며 내부 chain-of-thought를 저장하는 필드가 아닙니다.

## Rule Engine 기본 중단 조건

- 답변이 두 개 이상의 Branch와 매칭됨
- 새로운 질문이 포함됨
- 필요한 값이 누락됨
- 승인된 날짜·비용·범위를 벗어남
- 예상하지 못한 주제가 포함됨
- 사용자가 중간에 직접 개입함
- BATON-generated message임
- 플랫폼 연결이나 동기화 상태가 정상적이지 않음
- 이미 처리한 event 또는 이미 실행된 BATON임

## 데이터베이스

JPA는 스키마를 임의 생성하거나 변경하지 않고 검증만 수행합니다. 모든 스키마 변경은 Flyway migration으로 관리합니다. 상세 규칙은 [`docs/DATABASE.md`](docs/DATABASE.md)를 따릅니다.

## 관련 문서

- [`AGENTS.md`](AGENTS.md): 코드 작업 시 지켜야 할 규칙
- [`docs/DATABASE.md`](docs/DATABASE.md): PostgreSQL·Flyway 규칙
- [`CONTRIBUTING.md`](CONTRIBUTING.md): 브랜치·커밋·PR 규칙

## 관련 저장소

- Frontend: `Likelion-Yonsei-14th/14-HACKATHON-FRONTEND-BATON`

