# 기여 가이드

## 브랜치

- `main`: 배포 가능한 안정 버전
- `dev`: 통합 브랜치이자 기본 PR 대상
- `feature/{issue}-{summary}`: 기능 개발
- `fix/{issue}-{summary}`: 버그 수정
- `hotfix/{issue}-{summary}`: 운영 긴급 수정

새 작업은 원칙적으로 최신 `dev`에서 분기합니다. 브랜치 이름은 영문 소문자와 하이픈을 사용합니다.

## 커밋

```text
feat: 새로운 기능
fix: 버그 수정
refactor: 동작을 바꾸지 않는 구조 개선
test: 테스트 추가 또는 수정
docs: 문서 수정
style: 포맷 변경
chore: 설정과 도구 변경
```

한 커밋에는 하나의 논리적 변경을 담고, migration과 대응 entity 변경은 추적 가능한 형태로 구성합니다.

## Pull Request

- PR 대상은 기본적으로 `dev`입니다.
- 변경 이유, API 영향, DB 영향과 검증 방법을 작성합니다.
- API 계약 변경은 프론트엔드 저장소와 함께 확인합니다.
- migration이 있으면 `docs/DATABASE.md`의 확인 항목을 적용합니다.
- token, 운영 설정, 실제 사용자 데이터가 포함되지 않았는지 확인합니다.

## 머지 전 확인

```bash
./gradlew test
./gradlew build
```

