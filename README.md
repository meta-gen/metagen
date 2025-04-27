# 🤘MetaGen

MetaGen은 데이터 사전 또는 규칙 기반 명명법을 활용하여 메소드와 함수명을 자동으로 생성하고, 이를 기반으로 설계서 및 테스트 시나리오 작성을 지원하는 웹 애플리케이션입니다.  
MetaGen은 대규모 시스템 개발에서 일관된 명명 규칙을 준수하고, 문서화 작업을 자동화하여 개발 생산성을 극대화합니다.

[Docker Hub](https://hub.docker.com/repository/docker/koboolean/metagen)

---
## 기술 스택

### **Backend** (REST API 개발, 인증/인가, 비즈니스 로직)
- Spring Boot 3.4.1
- Java 17
- Spring Security 6.2.1
- JPA (Hibernate) 6.4.0.Final
- QueryDSL 5.0.0
- Lombok 1.18.30
- Spring Web (REST API)
- ModelMapper 3.1.0
- Spring Batch 5.2.1

### **Frontend** (UI 렌더링, 사용자 인터페이스, 동적 콘텐츠 처리)
- Thymeleaf 3.1.2
- Thymeleaf Layout Dialect 3.2.1
- jQuery 3.6.0
- Ajax (jQuery 기반)
- Bootstrap 5.3.2
- JavaScript ES6+
- Thymeleaf Spring Security Extras 3.1.2

### **Database**
- PostgreSQL 16.3
- PostgreSQL JDBC Driver 42.6.0

### **DevOps / CI-CD**
- Docker 24.0.7
- Docker Compose 2.20.2
- GitHub Actions, Jenkins 2.440.1

### **프로젝트 관리 & 문서화**
- Jira (Cloud)
- GitHub (Actions 포함)
- Springdoc OpenAPI (Swagger) 2.8.5

### **유틸리티**
- Apache POI (Excel 처리) 5.3.0
- POI-Ooxml, XmlBeans, POI-Ooxml-Schemas 포함
- Apache Commons Collections 4.4

### **도입 검토 중**
- **Redis Pub/Sub** → 승인관리 시스템 비동기 메시징 처리
#### **Search & Indexing** (검색 및 인덱싱)
- **Elasticsearch** 8.11.0 → 대량 데이터 검색 및 실시간 분석
- **Spring Data Elasticsearch** 5.2.1 → Elasticsearch와의 연동 및 데이터 저장/검색 관리
- **Spring Batch** 5.2.1 → 시간이 소요되는 Backend 로직 수행

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
## 포트

- **Spring Boot 서버 포트**: 기본적으로 `9940번`을 사용합니다.

---
## 데이터베이스 관리

- 데이터베이스는 **PostgreSQL**을 사용합니다.

### 개발 서버에 직접 설치
- PostgreSQL은 **15439** 포트를 사용합니다.

1. **PostgreSQL 설치**  
   PostgreSQL 공식 웹사이트에서 설치: [https://www.postgresql.org/download/](https://www.postgresql.org/download/)

2. **포트 변경 및 실행**
```bash
systemctl restart postgresql
```

---
### Docker로 설치
> MetaGen은 Postgres 형식과 H2 형식 두 가지를 제공합니다. H2 형식을 사용할 경우 아래 PostgreSQL 관련 설정은 무시하셔도 됩니다.

1. Docker Compose로 실행
```bash
cd docker
docker-compose -p metagen up -d
```

2. Docker 단독 명령어로 실행 시
```bash
docker volume create metagen
docker network create meta-network

# PostgreSQL
docker run -d \
  --name meta-gen-postgres \
  --network meta-network \
  -e POSTGRES_PASSWORD=meta-gen \
  -e POSTGRES_DB=meta-gen \
  -e POSTGRES_USER=meta-gen \
  -p 15439:5432 \
  -v ./volume/metagen:/var/lib/postgresql/data \
  postgres:16.3
```

3. H2 형식으로 실행 (테스트/로컬)
```bash
docker run -d \
  --name meta-gen-app \
  --network meta-network \
  -v ./volume/h2:/data \
  -p 9940:9940 \
  koboolean/metagen:h2-latest
```

> 기본 이미지는 HTTPS 환경을 기준으로 배포됩니다. HTTP 환경에서 실행하려면 `http-`가 포함된 태그를 사용해 주세요.

---
## 계정

1. 초기 관리자 계정 정보:
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
### ERD 구조
MetaGen의 주요 엔티티와 관계를 시각적으로 확인할 수 있습니다.
- 사용자(User), 명명 규칙(Rule), 메소드(Method), 테스트 시나리오(Scenario) 등으로 구성되어 있습니다.

🔗 [ERD Cloud - METAGEN](https://www.erdcloud.com/d/KE8576rcrNDyCve2h)

---
## 라이선스

이 프로젝트는 [MIT 라이선스](LICENSE)를 따릅니다.  
자유롭게 수정 및 배포할 수 있으나, 저작권 및 라이선스 명시가 필요합니다.
