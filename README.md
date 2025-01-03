
# 🤘MetaGen

MetaGen은 데이터 사전 또는 규칙 기반 명명법을 활용하여 메소드와 함수명을 자동으로 생성하고, 이를 기반으로 설계서 및 테스트 시나리오 작성을 지원하는 웹 애플리케이션입니다.
MetaGen은 특히 대규모 시스템 개발에서 일관된 명명 규칙 준수와 문서화의 생산성을 극대화하도록 설계되었습니다.

---

## 프로젝트 목표

1. **코드 일관성 향상**
    - 명명 규칙에 기반하여 메소드 및 함수명을 자동 생성하여 개발 과정의 일관성을 유지합니다.

2. **문서화 자동화**
    - 생성된 명명 규칙과 데이터를 기반으로 설계서 및 테스트 시나리오를 자동으로 생성하여 문서화의 효율성을 높입니다.

3. **개발 생산성 향상**
    - 템플릿 제공 및 자동화 기능을 통해 반복적인 작업을 줄이고 개발자들이 핵심 로직에 집중할 수 있도록 지원합니다.

---

## 주요 기능

1. **명명법 기반 메소드 및 함수명 생성**
    - 데이터 사전 또는 규칙에 기반하여 일관된 메소드 및 함수명을 생성합니다.
    - 사용자 정의 규칙도 지원합니다.

2. **코드 템플릿 제공**
    - 생성된 메소드 및 함수명에 맞는 코드 템플릿을 자동으로 제공합니다.
    - 다양한 언어와 프레임워크에 대한 템플릿 지원을 계획 중입니다.

3. **설계서 자동 생성**
    - 등록된 명명 규칙을 기반으로 설계서를 자동으로 생성하여 프로젝트 문서화 작업을 간소화합니다.

4. **엑셀 기반 테스트 시나리오 생성**
    - 엑셀 템플릿을 통해 테스트 시나리오를 자동으로 생성하고, 다양한 테스트 케이스를 관리할 수 있도록 지원합니다.

---

## 기술 스택

- **Backend**: Spring Boot, Java 17
- **Frontend**: Thymeleaf
- **Database**: PostgreSQL
- **Security**: Spring Security
- **Containerization**: Docker

---

## 포트

- **Spring Boot 서버 포트**: 기본적으로 `9940번`을 사용합니다.

---

## 데이터베이스 관리

- 데이터베이스는 **PostgreSQL**을 사용합니다.

### 개발 서버에 직접 설치
- 기본 포트 `5432`를 사용하지 않으며, **15439** 포트를 사용합니다.

1. **PostgreSQL 설치**  
   PostgreSQL 공식 웹사이트에서 설치: [https://www.postgresql.org/download/](https://www.postgresql.org/download/)

2. **포트 변경**  
   `postgresql.conf` 파일을 편집하여 포트를 **15439**로 변경한 후 재시작합니다:
   ```shell
   systemctl restart postgresql
   ```

---

### Docker로 설치
1. 프로젝트는 기본적으로 Docker Compose 파일을 제공합니다.
2. 프로젝트 `root` 디렉토리에서 `docker` 디렉토리로 이동한 후 다음 명령어를 실행합니다:
   ```shell
   cd docker
   docker compose up -d
   ```

---

### 설치 이후 설정
- PostgreSQL의 기본 **DB 스키마**, **사용자**, **비밀번호**는 다음과 같습니다:
    - DB Name: `meta-gen`
    - User: `meta-gen`
    - Password: `meta-gen`

- 설정 변경은 **application.yml**에서 가능합니다.
    - Docker 사용 시에는 `docker-compose.yml` 파일에서 다음 내용을 수정하세요:
      ```yaml
      POSTGRES_PASSWORD: meta-gen
      POSTGRES_DB: meta-gen
      POSTGRES_USER: meta-gen
      ```

---

## 계정

1. 초기 관리자 계정 정보는 다음과 같습니다:
    - Username: `admin`
    - Password: `admin`

2. 보안을 위해 초기 로그인 후 **내정보** 페이지에서 비밀번호를 변경해주세요.

---

## 설치 및 실행

### 사전 요구 사항
- Java 17 이상
- Docker (선택)

### 프로젝트 클론 및 설정
```bash
git clone https://github.com/username/metagen.git
cd metagen
```

### 애플리케이션 빌드 및 실행
```bash
./gradlew clean build
./gradlew bootRun
```

### 애플리케이션 접속
- 브라우저에서 [http://localhost:9940](http://localhost:9940)로 접속합니다.


---

## 라이선스

이 프로젝트는 [MIT 라이선스](LICENSE)를 따릅니다.
