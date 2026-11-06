# 작업 필요사항

## JDK 21 / Spring Boot 4.1 업그레이드 (AI 기능 도입 전 선행)

> 분석일: 2026-11-06 · 현재: JDK 17 / Spring Boot 3.4.1 / Gradle 8.11.1
> 판단: **변경 가능 (차단 요소 없음)** · 예상 작업량 1~2일

### 배경

- **Spring AI 2.0** (2026-06 GA)은 Spring Boot 4.0 / 4.1 전용이며 Java 21 이상이 필요하다. Boot 3.x에서는 사용할 수 없다.
- Spring Boot 3.4.x는 OSS 지원이 종료되었다. 4.1.x는 2027-07, 4.0.x는 2026-12까지 지원되므로 **4.1.x를 목표**로 한다.
- JDK 21의 가상 스레드(`spring.threads.virtual.enabled: true`)는 LLM 호출처럼 대기 시간이 긴 요청에 유리하다.

### 진행 순서

1. [ ] **스모크 테스트 작성** (현재 `src/test` 없음)
   - 로그인 2단계(`POST /api/login/projects` → 폼 로그인), 주요 화면 13개, 주요 API, 채팅 웹소켓, 시스템 로그 저장
   - 지금까지 수동으로 확인한 항목을 테스트 코드로 옮겨 각 단계마다 회귀를 확인한다.
2. [ ] **Spring Boot 3.5.x + JDK 21** (공식 가이드 권장: 4.0 전에 최신 3.5.x 경유)
   - `build.gradle` toolchain / source / targetCompatibility → 21
   - Dockerfile 베이스 이미지 `openjdk:17-jdk-slim`(더 이상 관리되지 않음) → `eclipse-temurin:21`
   - deprecated 경고 정리 (3.x에서 deprecated된 API는 4.0에서 삭제됨)
3. [ ] **사전 정리** (3.5에서 미리 처리해 4.x 전환 위험 축소)
   - [ ] **Spring Batch 제거**: 로그만 출력하는 `batchJob` 하나뿐이고 실행도 비활성(`job.enabled: false`).
     Batch 6은 설정 방식과 시퀀스명(`BATCH_JOB_SEQ` → `BATCH_JOB_INSTANCE_SEQ`)이 바뀐다.
     제거 대상: `batch/config/BatchConfig`, `MetaGenApplication`의 `@BatchDataSource`, `spring.batch.*` 설정, 스타터 의존성
     (Batch 메타 테이블 DROP은 새 Flyway 마이그레이션으로)
   - [ ] **QueryDSL → OpenFeign 포크(7.x)**: 원본 `com.querydsl 5.0.0`은 2021년 이후 릴리스가 없다. OpenFeign 7.x는 Hibernate 7을 지원한다.
     사용 파일 3개. `build.gradle`의 `dependencyManagement.importedProperties['querydsl.version']` 참조도 명시 버전으로 교체
4. [ ] **Spring Boot 4.1.x**
   - [ ] Gradle wrapper 업그레이드 (Boot 4 플러그인 최소 버전 확인)
   - [ ] **`spring-boot-starter-flyway` 추가 (필수)**: Boot 4는 모듈화되어 `flyway-core`만으로는 자동 설정이 동작하지 않는다.
     누락 시 마이그레이션이 **오류 없이 실행되지 않고**, 새 DB에서는 `ddl-auto: validate` 실패로 기동되지 않는다.
   - [ ] `spring-boot-starter-web` → `spring-boot-starter-webmvc` (기존 이름도 당분간 동작하지만 deprecated)
     우선 기존 이름의 통합(classic) 스타터로 올린 뒤 기능별 스타터로 정리
   - [ ] **Jackson 3**: `logs/util/SensitiveDataMasker`의 `com.fasterxml.jackson.databind` → `tools.jackson.databind`
     (`@JsonIgnore` 등 annotation 패키지는 그대로). `build.gradle`의 `jackson-databind`, `jackson-datatype-jsr310` 직접 의존성 삭제
   - [ ] springdoc `2.8.5` → `3.x`
   - [ ] H2 콘솔: 별도 모듈 필요 여부 확인 (`h2-local`에서만 사용)
   - [ ] Thymeleaf Spring Security 연동: Security 7 공식 모듈이 아직 없고 `thymeleaf-extras-springsecurity6`가 동작한다는 사용자 보고만 있다.
     사용처는 `sec:authorize`(isAuthenticated / isAnonymous)와 `#authentication.principal.id` 3곳뿐이라 문제 시 모델 속성으로 대체
   - [ ] Spring Security 7 / Framework 7 경로 패턴 규칙 확인: `LogsConfig`의 `/api/**/data`, `/api/**/column` 같은 중간 `**` 패턴 허용 여부
5. [ ] **Hibernate 7 스키마 검증**
   - Hibernate 7로 DDL을 다시 생성해 `db/migration/{postgresql,h2}/V1__init_schema.sql`과 비교
   - 차이가 있으면 V1은 수정하지 말고 `V{n}__*.sql`을 **두 vendor 폴더 모두에** 추가
   - 새 Postgres / 기존 Postgres(baseline) / H2 각각에서 기동 확인
6. [ ] **Spring AI 2.0 도입**

### 영향 없음 (버전 확인만)

WebSocket / SockJS, Spring Security 설정(lambda DSL 사용, `WebSecurityConfigurerAdapter`·`.and()` 없음), `RoleHierarchyImpl.fromHierarchy`, devtools, Lombok, Apache POI, ModelMapper, Caffeine, `javax.*` 사용 없음

### 참고

- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Boot 4.0.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0.0-Release-Notes)
- [Modularizing Spring Boot](https://spring.io/blog/2025/10/28/modularizing-spring-boot)
- [Spring AI 2.0.0 GA](https://spring-io-website.pages.dev/blog/2026/06/12/spring-ai-2-0-0-GA-available-now)
- [What's new in Spring Batch 6](https://docs.spring.io/spring-batch/reference/whatsnew.html)
- [springdoc-openapi](https://github.com/springdoc/springdoc-openapi)
- [OpenFeign/querydsl Releases](https://github.com/OpenFeign/querydsl/releases)
- [thymeleaf-extras-springsecurity #97 (Spring Security 7 지원)](https://github.com/thymeleaf/thymeleaf-extras-springsecurity/issues/97)
