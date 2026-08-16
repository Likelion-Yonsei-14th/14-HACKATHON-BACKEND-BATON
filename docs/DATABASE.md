# 데이터베이스와 Flyway 가이드

## 기본 원칙

- PostgreSQL schema는 Flyway migration으로만 변경합니다.
- JPA `ddl-auto`는 `validate`를 사용합니다.
- 애플리케이션 시작 시 entity와 schema가 다르면 즉시 실패하도록 유지합니다.
- 운영·개발·테스트 환경이 같은 migration 이력을 사용하도록 합니다.

## 파일 위치와 이름

Migration 파일은 다음 경로에 둡니다.

```text
src/main/resources/db/migration/
```

이름은 아래 형식을 사용합니다.

```text
V{순번}__{snake_case_설명}.sql
```

예:

```text
V1__create_initial_schema.sql
V2__add_baton_expiration.sql
V3__add_message_event_unique_constraint.sql
```

## 새 migration 작성 절차

1. 기본 브랜치의 최신 migration 순번을 확인합니다.
2. 마지막 순번 다음 번호로 새 SQL 파일을 만듭니다.
3. entity, index, constraint와 nullability가 일치하는지 확인합니다.
4. 깨끗한 PostgreSQL에서 전체 migration을 실행합니다.
5. 기존 schema에서 새 버전으로 업그레이드되는지 확인합니다.
6. 애플리케이션을 실행해 JPA validation과 테스트를 통과시킵니다.

## 금지 사항

- 이미 `dev` 또는 `main`에 병합된 migration 수정
- 적용된 migration의 이름이나 순번 변경
- 기존 파일을 삭제하고 같은 순번으로 다시 생성
- 로컬·운영 DB에서만 직접 DDL을 실행하고 migration을 남기지 않는 행위
- entity만 수정하고 migration을 누락하는 행위
- 환경마다 서로 다른 schema 변경 절차를 사용하는 행위

적용된 schema를 변경해야 하면 기존 파일을 고치지 말고 새로운 migration을 추가합니다.

## Schema 작성 규칙

- 테이블과 컬럼은 `snake_case`를 사용합니다.
- PK, FK, unique constraint와 주요 index의 이름을 명시합니다.
- `NOT NULL` 여부와 기본값을 의도적으로 결정합니다.
- 시간 값은 timezone 정책을 명확히 하고 UTC 기준으로 저장합니다.
- 상태 값은 애플리케이션 enum과 migration 정의가 일치해야 합니다.
- 외부 event와 Action 멱등성에 필요한 unique constraint를 DB에서도 보장합니다.
- FK 삭제 동작은 기본값에 맡기지 말고 `RESTRICT`, `CASCADE`, `SET NULL` 중 의도를 검토합니다.

## 안전한 변경 순서

운영 중인 컬럼 변경은 가능한 한 확장과 축소를 분리합니다.

```text
새 nullable 컬럼 또는 새 테이블 추가
→ 애플리케이션이 구·신 schema와 호환되도록 배포
→ 데이터 backfill
→ 읽기 경로 전환
→ 후속 migration에서 NOT NULL 또는 기존 컬럼 제거
```

대용량 테이블의 index, backfill, constraint 추가는 lock 시간과 배포 영향을 검토합니다.

## Rollback 원칙

- 운영 rollback은 migration 파일을 되돌려 재사용하는 방식으로 처리하지 않습니다.
- 필요한 경우 상태를 복구하는 forward migration을 새로 작성합니다.
- 파괴적인 변경 전에는 복구 방법과 데이터 백업 여부를 확인합니다.

## Pull Request 확인 항목

- [ ] migration 순번이 최신 `dev`와 충돌하지 않음
- [ ] 이미 병합된 migration을 수정하지 않음
- [ ] entity와 schema가 일치함
- [ ] 필요한 index와 constraint가 포함됨
- [ ] clean migration과 upgrade migration을 모두 확인함
- [ ] 데이터 손실 또는 table lock 위험을 설명함
- [ ] 관련 테스트와 문서를 갱신함

