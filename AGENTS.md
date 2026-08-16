# BATON Backend 작업 지침

이 문서는 이 저장소에서 작업하는 코딩 에이전트와 개발자가 따라야 할 규칙입니다. 사용자의 명시적인 요청과 저장소의 실제 코드·설정이 이 문서보다 우선합니다.

## 작업 전 확인

1. 관련 controller, service, entity, repository, migration과 테스트를 먼저 확인합니다.
2. `README.md`, `docs/DATABASE.md`, `.env.example`의 현재 기준을 확인합니다.
3. 요청과 관계없는 파일이나 사용자의 기존 변경을 수정하지 않습니다.
4. Java와 Spring 버전은 Gradle 설정을 기준으로 판단합니다.

## 절대 변경하면 안 되는 제품 원칙

- LLM이 새로운 결정, 약속, 일정, 비용 또는 범위를 만들게 하지 않습니다.
- LLM의 출력만으로 Action을 실행하지 않습니다. 모든 실행은 Rule Engine과 guardrail을 통과해야 합니다.
- 모든 Branch는 `Condition → Decision → Action`을 함께 저장합니다.
- 하나의 BATON은 최대 한 번만 자동 실행됩니다.
- 사람이 직접 개입하면 기존 BATON을 일시정지하거나 취소합니다.
- `is_baton_generated=true`인 메시지는 다른 BATON을 작동시키지 않습니다.
- 애매함, 새 질문, 복수 매칭, 범위 초과, 값 누락, 연결 오류는 자동 실행하지 않습니다.
- 판정, 검증, 실행 결과는 audit 가능한 형태로 남깁니다.

## 계층과 책임

- Controller는 입력 검증과 응답 변환에 집중합니다.
- Service는 유스케이스와 transaction 경계를 담당합니다.
- Repository는 영속성 접근만 담당합니다.
- 외부 플랫폼과 AI 연동 코드는 domain service에서 분리합니다.
- Rule Engine은 가능한 한 순수하고 결정적인 로직으로 작성합니다.
- ActionExecutor는 검증 완료된 명령만 받도록 인터페이스를 제한합니다.
- entity를 API 응답으로 직접 반환하지 않습니다.

## 메시지와 실행 안전성

- 외부 event는 `external_event_id` 등을 이용해 멱등 처리합니다.
- BATON 조회 시 `original_message_id`, `thread_id`, `conversation_id`를 모두 검증합니다.
- 실행 전에 BATON 상태, 만료, 사람의 개입, 기존 execution을 다시 확인합니다.
- 동시 요청으로 같은 BATON이 두 번 실행되지 않도록 DB constraint, lock 또는 원자적 상태 전이를 사용합니다.
- 외부 Action 성공 여부를 확인하기 전에 내부 상태를 성공으로 확정하지 않습니다.
- 재시도 가능한 실패와 영구 실패를 구분해 기록합니다.

## LLM 연동

- 응답은 schema로 검증 가능한 structured output으로 받습니다.
- confidence 하나만으로 실행을 허용하지 않습니다.
- 모델의 `selectedBranchId`가 해당 BATON의 승인된 Branch인지 서버에서 검증합니다.
- 추출한 날짜, 수량, 비용 등은 서버 규칙으로 다시 검증합니다.
- 내부 chain-of-thought를 요청하거나 저장하지 않습니다. 사용자용 짧은 `reasoningSummary`만 허용합니다.
- prompt와 model 변경에는 회귀 테스트 또는 고정된 평가 사례를 함께 검토합니다.

## 데이터베이스와 Flyway

- `ddl-auto`는 `validate`를 사용하고 schema 변경은 Flyway가 전담합니다.
- 이미 공유되거나 적용된 migration 파일을 수정하지 않습니다.
- 변경은 항상 새로운 migration 파일로 추가합니다.
- migration 이름과 작성 규칙은 `docs/DATABASE.md`를 따릅니다.
- entity 변경에는 대응 migration과 관련 테스트가 포함되어야 합니다.
- 로컬 DB를 수동 변경해 문제를 숨기지 않습니다.

## 시간 처리

- 저장과 서버 간 전달은 UTC와 ISO 8601을 기본으로 합니다.
- 사용자·상대방 timezone은 별도 값으로 보존합니다.
- 만료, 예약 발송, 근무시간 판정에서는 시스템 기본 timezone에 의존하지 않습니다.
- 시간 관련 테스트는 고정된 `Clock`을 주입해 재현 가능하게 작성합니다.

## 검증

변경 범위에 맞게 다음 명령을 실행합니다.

```bash
./gradlew test
./gradlew build
```

- 상태 전이와 Rule Engine 변경에는 단위 테스트를 추가합니다.
- webhook, DB transaction, 외부 Action 경계에는 통합 테스트를 우선 검토합니다.
- migration은 깨끗한 DB와 기존 schema 업그레이드 경로에서 검증합니다.
- 명령이 아직 구성되지 않았거나 실행할 수 없으면 그 사실을 결과에 명시합니다.

## 보안

- access token, refresh token, API key, signing secret과 실제 메시지를 로그에 남기지 않습니다.
- token은 평문으로 저장하지 않고 애플리케이션의 암호화 경계를 통과시킵니다.
- Slack webhook은 signature와 timestamp를 검증한 뒤 처리합니다.
- `.env`, 운영 설정, 인증서와 개인키를 커밋하지 않습니다.
- 테스트 fixture에는 실제 개인정보나 workspace 식별자를 사용하지 않습니다.

## 문서 동기화

실행 명령, 환경변수, 상태 전이, migration 정책 또는 API 계약이 달라지면 관련 README와 예시 파일도 함께 갱신합니다.

