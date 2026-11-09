<p align="center">
  <img src="metagen-app/src/main/resources/static/images/favicon.png" alt="MetaGen 로고" width="120" height="120">
</p>

<h1 align="center">MetaGen</h1>

MetaGen은 데이터 표준(표준 단어·도메인·용어)과 규칙 기반 명명법으로 테이블/컬럼명과 메소드·함수명을 일관되게 만들고, 이를 바탕으로 DDL·설계서·테스트 시나리오 작성을 지원하는 웹 애플리케이션입니다.
대규모 시스템 개발에서 명명 규칙을 지키고 반복적인 문서화 작업을 자동화해 개발 생산성을 높입니다.

[Docker Hub](https://hub.docker.com/repository/docker/koboolean/metagen) · [Document Page](https://metagen-react.vercel.app/) · [ERD Cloud](https://www.erdcloud.com/d/KE8576rcrNDyCve2h)

---

## 프로젝트 목표

1. **코드 일관성 향상** : 데이터 표준과 명명 규칙에 따라 테이블·컬럼·메소드명을 생성해 개발 과정의 일관성을 유지합니다.
2. **문서화 자동화** : 등록된 표준과 코드 규칙으로 DDL, 설계서, 테스트 시나리오를 생성합니다.
3. **개발 생산성 향상** : 엑셀 템플릿과 자동화 기능으로 반복 작업을 줄여 핵심 로직에 집중할 수 있도록 합니다.

---

## 주요 기능

| 메뉴 | 화면 | 내용 |
|---|---|---|
| 대시보드 | 대시보드 | 승인 대기 현황, 최근 활동, 공지사항 |
| 운영 관리 | 공지사항 | 프로젝트별 공지 등록·조회 (읽음 표시) |
| 시스템 관리 | 사용자 / 프로젝트 / 시스템 로그 | 사용자 승인·권한, 프로젝트 설정(테이블명 접두어, 대상 DB), API 요청 로그 |
| 데이터 관리 | 데이터 사전 | 표준 단어·도메인·용어 등록/승인, 엑셀 업로드·다운로드 (공통표준용어 템플릿 제공) |
| | 테이블 관리 | 한글명을 표준 단어 영문약어로 변환해 테이블명 생성, 테이블 상세·DDL 보기 |
| | 컬럼 관리 | 표준용어 기반 컬럼 등록, 컬럼 일괄 편집, 공통 컬럼 세트, 엑셀 업로드 |
| 설계기반 관리 | 코드규칙 관리 / 코드 규칙 | 명명 규칙 템플릿 관리, 데이터 사전 기반 메소드·함수명 생성 |
| | 설계서 | 기능 정의서·인터페이스 설계서·기술 설계서 엑셀 출력 |
| | 테스트 시나리오 | 단위·통합 테스트 시나리오 엑셀 출력 |
| 메신저 | (우측 하단) | 로그인 사용자 목록과 1:1 실시간 채팅 (STOMP WebSocket) |

### 데이터 표준 규칙
- 표준용어명과 영문약어명은 단어 구분자 `_` 로 연결하며, 두 이름의 단어 수가 같아야 합니다. (예: `사용_내역_관리` / `USE_DTLS_MNG`)
- `_` 는 단어 구분자이므로 **표준 단어명/영문약어명에는 사용할 수 없습니다.**
- 영문약어명 검색은 대소문자를 구분하지 않습니다.

### DDL 생성
- 프로젝트 관리에서 대상 DB를 지정합니다: **PostgreSQL(기본) / Oracle / MySQL / MariaDB**
- 테이블 상세 > DDL 보기에서 `CREATE TABLE`, PK/UNIQUE 제약, 인덱스, 주석, FK를 생성하고 길이 미지정·PK 없음 등의 경고를 표시합니다.

---

## 권한

권한은 계층 구조이며, 상위 권한은 하위 권한의 메뉴와 기능을 모두 사용할 수 있습니다. 접근할 수 없는 메뉴는 사이드바에 표시되지 않습니다.

```
ADMIN > MANAGER > DBA > USER > VIEWER > NOT_APPROVE
```

| 권한 | 설명 | 주요 사용 메뉴 |
|---|---|---|
| `ADMIN` | 관리자 | 사용자 관리 포함 전체 |
| `MANAGER` | 매니저 | 승인, 공지사항 등록·수정 |
| `DBA` | DBA | 프로젝트 관리, 시스템 로그, 데이터 사전 승인 |
| `USER` | 사용자 | 컬럼 관리, 코드 규칙, 설계서, 테스트 시나리오 |
| `VIEWER` | 뷰어 | 데이터 사전·테이블·코드규칙 조회 |
| `NOT_APPROVE` | 권한 미승인 (회원가입 직후) | 대시보드, 공지사항 |

---

## 기술 스택

| 구분 | 내용 |
|---|---|
| Language / Build | Java 21, Gradle 9.8 (Wrapper 포함, Gradle 멀티모듈) |
| Backend | Spring Boot 4.1, Spring Security, Spring Data JPA, QueryDSL 7 (OpenFeign), WebSocket(STOMP) |
| DB | PostgreSQL 18 (기본) / H2 (로컬·테스트), Flyway 마이그레이션 |
| Frontend | Thymeleaf + Layout Dialect, jQuery, DataTables, GitHub Primer 스타일, Pretendard 폰트 |
| 기타 | Apache POI 5 (엑셀 SAX/SXSSF 스트리밍), Caffeine, springdoc-openapi |

### 모듈 구성

```
의존 방향: common ← core ← (system, operation, data, chat) ← design(→ data) ← app
```

| 모듈 | 역할 |
|---|---|
| `metagen-common` | 공통 기반 (BaseEntity, 그리드, 예외, 유틸) |
| `metagen-core` | 인증/로그인, 프로젝트 테넌시, 요청 로그 |
| `metagen-system` | 시스템 관리 (사용자/프로젝트/시스템 로그) |
| `metagen-operation` | 운영 관리 (공지사항) |
| `metagen-data` | 데이터 관리 (데이터 사전/테이블/컬럼/공통 컬럼) |
| `metagen-design` | 설계기반 관리 (코드 규칙/설계서/테스트 시나리오) |
| `metagen-chat` | 메신저 |
| `metagen-app` | 실행 모듈 (`metagen.jar`, 대시보드, 화면 리소스, 설정, Flyway 마이그레이션) |

---

## 실행 환경 (최소 요구 사항)

| 항목 | 버전 |
|---|---|
| Java (JDK) | **21 이상** |
| PostgreSQL | 18 권장 (Docker Compose 기준 `postgres:18.6`) — H2 프로필 사용 시 불필요 |
| Docker | 선택 (Docker Compose v2) |
| 포트 | 애플리케이션 `9940`, PostgreSQL `15439` |

> Gradle은 Wrapper(`./gradlew`)를 사용하므로 별도 설치가 필요 없습니다.

---

## 설치 및 실행

### 1. 프로젝트 클론
```bash
git clone https://github.com/meta-gen/metagen.git
cd metagen
```

### 2. 데이터베이스 준비 (PostgreSQL)
```bash
cd docker
docker-compose -p metagen up -d
```
- `localhost:15439` 에 DB/계정 `meta-gen` / 비밀번호 `meta-gen` 으로 PostgreSQL이 실행됩니다.
- PostgreSQL을 직접 설치하는 경우 [공식 사이트](https://www.postgresql.org/download/)에서 설치 후 포트를 `15439`로 변경하고 위와 같은 DB/계정을 만들어 주세요.

### 3. 빌드 및 실행
```bash
./gradlew clean build      # 빌드 + 테스트 (테스트 제외: ./gradlew build -x test)
./gradlew bootRun          # local 프로필 (PostgreSQL localhost:15439)

# PostgreSQL 없이 H2 In-Memory DB로 실행 (기동할 때마다 초기화)
./gradlew bootRun --args='--spring.profiles.active=h2-local'
```

빌드 결과물은 `metagen-app/build/libs/metagen.jar` 입니다.
```bash
java -jar metagen-app/build/libs/metagen.jar --spring.profiles.active=local
```

### 4. 접속
- [http://localhost:9940](http://localhost:9940)
- 로컬 프로필(`local`, `h2-local`)에서만 Swagger UI([/swagger-ui](http://localhost:9940/swagger-ui))와 H2 콘솔(`/h2-console`, `h2-local`)이 활성화됩니다.

### 프로필

| 프로필 | DB | 용도 |
|---|---|---|
| `local` (기본) | PostgreSQL `localhost:15439` | 로컬 개발 |
| `h2-local` | H2 In-Memory | 로컬 개발 (DB 설치 불필요) |
| `dev` | PostgreSQL `meta-gen-postgres:5432` | 개발 서버 (포트 18080) |
| `docker` | PostgreSQL (`DB_*` 환경변수) | Docker 이미지 |
| `h2-docker` | H2 파일 DB (`/data`) | Docker 이미지 (단독 실행) |

---

## Docker로 실행

> HTTP / HTTPS 환경 모두 같은 이미지를 사용합니다. 요청이 HTTPS이면 세션 쿠키에 `Secure`가 자동으로 붙고,
> nginx 등 리버스 프록시에서 HTTPS를 처리하는 경우 `X-Forwarded-Proto` 헤더를 전달해 주세요. (기존 `http-` 태그는 더 이상 필요하지 않습니다.)

| 이미지 태그 | Dockerfile | 프로필 |
|---|---|---|
| `koboolean/metagen:latest` | `Dockerfile` | `docker` (PostgreSQL) |
| `koboolean/metagen:h2-latest` | `Dockerfile-h2` | `h2-docker` (H2 파일 DB) |

이미지는 `eclipse-temurin:21-jdk` 기반입니다.

### PostgreSQL + 애플리케이션 (Docker Compose)
```bash
cd docker/metagen
docker-compose -p metagen up -d
```

### Docker 단독 명령어
```bash
docker network create meta-network

# PostgreSQL 18 (18부터 데이터 볼륨 경로가 /var/lib/postgresql 입니다)
docker run -d \
  --name meta-gen-postgres \
  --network meta-network \
  -e POSTGRES_PASSWORD=meta-gen \
  -e POSTGRES_DB=meta-gen \
  -e POSTGRES_USER=meta-gen \
  -p 15439:5432 \
  -v ./volume/metagen:/var/lib/postgresql \
  postgres:18.6

# 애플리케이션 (DB 접속 정보: DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD)
docker run -d \
  --name meta-gen-app \
  --network meta-network \
  -e DB_HOST=meta-gen-postgres \
  -p 9940:9940 \
  koboolean/metagen:latest
```

### H2 형식 (DB 없이 단독 실행)
```bash
docker run -d \
  --name meta-gen-app \
  -v ./volume/h2:/data \
  -p 9940:9940 \
  koboolean/metagen:h2-latest
```

---

## 데이터베이스 관리

- 스키마와 초기 데이터는 **Flyway**가 관리합니다. (`metagen-app/src/main/resources/db/migration/{postgresql,h2}`)
- 애플리케이션 기동 시 미적용 마이그레이션이 자동으로 실행되며, JPA는 스키마 검증(`validate`)만 수행합니다.
- Flyway 도입 전 데이터베이스도 자동으로 baseline 처리되어 그대로 사용할 수 있습니다.
- 일부 마이그레이션(V6: 용어↔컬럼 조인 테이블 제거 등)은 테이블 구조를 변경하므로, **운영 DB를 업그레이드하기 전에는 백업을 권장합니다.**

---

## 계정

1. 초기 관리자 계정
    - Username: `admin`
    - Password: `admin`
2. 초기 비밀번호로는 바로 로그인할 수 없습니다. 첫 로그인 시 표시되는 **비밀번호 변경** 창에서 비밀번호를 변경한 뒤 로그인해 주세요.
   (관리자가 비밀번호를 초기화한 계정도 동일합니다.)
3. 로그인은 아이디/비밀번호 확인 후 **접근 가능한 프로젝트를 선택**하는 순서로 진행됩니다.
4. 회원가입한 사용자는 `NOT_APPROVE` 권한으로 시작하며, 관리자가 권한을 부여해야 메뉴를 사용할 수 있습니다.

---

## 라이선스

이 프로젝트는 [MIT 라이선스](LICENSE)를 따릅니다.
자유롭게 수정 및 배포할 수 있으나, 저작권 및 라이선스 명시가 필요합니다.
