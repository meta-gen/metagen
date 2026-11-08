package com.koboolean.metagen;

import com.koboolean.metagen.core.logs.domain.entity.Logs;
import com.koboolean.metagen.core.logs.repository.LogsRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.SimpleMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 스모크 테스트 : 실제 서버(랜덤 포트)를 띄워 로그인 → 주요 화면/API → 채팅 웹소켓 → 시스템 로그 저장까지 확인한다.
 * <p>
 * 프레임워크 업그레이드(Boot 3.5 / 4.x) 시 테스트 자동 설정 모듈 변경의 영향을 받지 않도록
 * MockMvc/TestRestTemplate 대신 JDK HttpClient를 사용한다.
 * 동시 세션이 1개로 제한(maximumSessions(1))되어 있으므로 클래스 단위로 한 번만 로그인하고 종료 시 로그아웃한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SmokeTest {

    private static final String USERNAME = "admin";
    /** 초기 비밀번호 (V2 초기 데이터) : 변경 전에는 로그인할 수 없다 */
    private static final String INITIAL_PASSWORD = "admin";
    /** 로그인 전 비밀번호 변경 단계에서 바꾼 비밀번호 */
    private static final String PASSWORD = "Smoke!Pass1";
    private static final long PROJECT_ID = 0L;

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private LogsRepository logsRepository;

    @Autowired
    private jakarta.persistence.EntityManagerFactory entityManagerFactory;

    private CookieManager cookieManager;
    private HttpClient client;
    private String loginUserId;

    @BeforeAll
    void login() throws Exception {
        cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // 1. 로그인 화면 : CSRF 토큰 쿠키 발급
        assertThat(get("/login", false).statusCode()).isEqualTo(200);
        assertThat(csrfToken()).isNotBlank();

        // 2. 초기 비밀번호(admin/admin)로는 프로젝트 조회 단계에서 차단 → 비밀번호 변경 요구
        HttpResponse<String> blocked = postJson("/api/login/projects",
                "{\"username\":\"" + USERNAME + "\",\"password\":\"" + INITIAL_PASSWORD + "\"}");
        assertThat(blocked.statusCode()).isEqualTo(403);
        assertThat(blocked.body()).contains("PASSWORD_CHANGE_REQUIRED");

        // 화면을 거치지 않고 폼 로그인을 직접 호출해도 차단된다
        HttpResponse<String> blockedLogin = postForm("/login",
                "username=" + USERNAME + "&password=" + INITIAL_PASSWORD + "&project_id=" + PROJECT_ID);
        assertThat(blockedLogin.statusCode()).isEqualTo(302);
        assertThat(location(blockedLogin)).contains("/login?error=true");

        // 3. 로그인 전 비밀번호 변경 (규칙 위반은 거부)
        assertThat(postJson("/api/login/password", "{\"username\":\"" + USERNAME + "\",\"password\":\"" + INITIAL_PASSWORD
                + "\",\"newPassword\":\"weak\"}").statusCode()).isEqualTo(400);
        HttpResponse<String> changed = postJson("/api/login/password", "{\"username\":\"" + USERNAME + "\",\"password\":\"" + INITIAL_PASSWORD
                + "\",\"newPassword\":\"" + PASSWORD + "\"}");
        assertThat(changed.statusCode()).as(changed.body()).isEqualTo(200);

        // 4. 새 비밀번호로 로그인 가능 프로젝트 조회
        HttpResponse<String> projects = postJson("/api/login/projects",
                "{\"username\":\"" + USERNAME + "\",\"password\":\"" + PASSWORD + "\"}");
        assertThat(projects.statusCode()).isEqualTo(200);
        assertThat(projects.body()).contains("\"projectName\":\"MAIN\"");

        // 5. 폼 로그인 (선택한 프로젝트 포함) : 비밀번호 변경을 마쳤으므로 대시보드로 이동
        HttpResponse<String> login = postForm("/login",
                "username=" + USERNAME + "&password=" + PASSWORD + "&project_id=" + PROJECT_ID);
        assertThat(login.statusCode()).isEqualTo(302);
        assertThat(location(login)).endsWith("/");

        // 대시보드 렌더링 확인 겸 로그인 사용자 ID(#authentication.principal.id) 확보
        HttpResponse<String> dashboard = get("/", false);
        assertThat(dashboard.statusCode()).isEqualTo(200);
        Matcher matcher = Pattern.compile("id=\"hidden-user-id\"[^>]*value=\"([^\"]+)\"").matcher(dashboard.body());
        assertThat(matcher.find()).as("hidden-user-id").isTrue();
        loginUserId = matcher.group(1);
    }

    @AfterAll
    void logout() throws Exception {
        if (client != null) {
            get("/logout", false);
        }
    }

    @Test
    @Order(1)
    @DisplayName("비로그인 요청 : AJAX는 401, 일반 요청은 /login 리다이렉트")
    void anonymousAccess() throws Exception {
        HttpClient anonymous = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

        HttpResponse<String> ajax = anonymous.send(HttpRequest.newBuilder(uri("/api/selectTable/column"))
                .header("X-Requested-With", "XMLHttpRequest").GET().build(), HttpResponse.BodyHandlers.ofString());
        assertThat(ajax.statusCode()).isEqualTo(401);

        HttpResponse<String> page = anonymous.send(HttpRequest.newBuilder(uri("/tableManage")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(page.statusCode()).isEqualTo(302);
        assertThat(location(page)).endsWith("/login");
    }

    @Test
    @Order(2)
    @DisplayName("접근 권한이 없는 프로젝트로는 로그인할 수 없다")
    void loginWithInaccessibleProject() throws Exception {
        CookieManager otherCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpClient other = HttpClient.newBuilder().cookieHandler(otherCookies)
                .followRedirects(HttpClient.Redirect.NEVER).build();

        // 비로그인 상태의 로그인 / 회원가입 화면 렌더링
        for (String path : new String[]{"/login", "/signup"}) {
            HttpResponse<String> page = other.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(page.statusCode()).as(path).isEqualTo(200);
            assertThat(page.body()).as(path).contains("class=\"auth-box\"").contains("</html>");
        }
        String token = cookie(otherCookies, "XSRF-TOKEN");

        HttpResponse<String> login = other.send(HttpRequest.newBuilder(uri("/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("username=" + USERNAME + "&password=" + PASSWORD
                        + "&project_id=99999&_csrf=" + token))
                .build(), HttpResponse.BodyHandlers.ofString());

        assertThat(login.statusCode()).isEqualTo(302);
        assertThat(location(login)).contains("/login?error=true");
    }

    @ParameterizedTest(name = "화면 {0}")
    @ValueSource(strings = {
            "/", "/account", "/notice", "/dataDictionary", "/tableManage", "/columnManage",
            "/codeRule", "/codeRuleManage", "/designManage", "/testManage",
            "/userManage", "/systemLog", "/projectManage", "/help", "/denied"
    })
    @Order(10)
    @DisplayName("주요 화면 렌더링")
    void pages(String path) throws Exception {
        HttpResponse<String> response = get(path, false);

        assertThat(response.statusCode()).as(path).isEqualTo(200);
        assertThat(response.body()).as(path).contains("</html>").contains("id=\"popupDialog\"");
    }

    @ParameterizedTest(name = "팝업 다이얼로그 조각 {0}")
    @ValueSource(strings = {
            "/popup/columTableSearch/detail", "/popup/standardTermCreate", "/popup/tableDdl", "/popup/commonColumnSet",
            "/popup/codeRulePopup", "/popup/codeRuleDetailPopup"
    })
    @Order(10)
    @DisplayName("팝업은 다이얼로그에 넣을 화면 조각만 반환한다 (html/head 없음)")
    void popupFragments(String path) throws Exception {
        assertPopupFragment(path, get(path, true));
    }

    private static void assertPopupFragment(String path, HttpResponse<String> response) {
        assertThat(response.statusCode()).as(path).isEqualTo(200);
        assertThat(response.body()).as(path)
                .contains("popup-container")
                .doesNotContain("<html")
                .doesNotContain("<head>");
    }

    @Test
    @Order(11)
    @DisplayName("공지사항 등록 → 상세/등록/수정 팝업 다이얼로그 조각 렌더링")
    void noticePopups() throws Exception {
        String title = "smoke-notice-" + UUID.randomUUID();
        HttpResponse<String> save = postJson("/api/saveNotice",
                "{\"title\":\"" + title + "\",\"content\":\"공지 내용\",\"projectIds\":[" + PROJECT_ID + "]}");
        assertThat(save.statusCode()).isEqualTo(200);

        HttpResponse<String> list = get("/api/selectNotice/data?page=0&size=100", true);
        String noticeId = null;
        for (tools.jackson.databind.JsonNode row : tools.jackson.databind.json.JsonMapper.builder().build()
                .readTree(list.body()).path("data")) {
            if (title.equals(row.path("title").asString())) {
                noticeId = row.path("id").asString();
            }
        }
        assertThat(noticeId).as("등록한 공지사항이 목록에 있어야 한다").isNotBlank();

        for (String path : new String[]{
                "/popup/noticePopupDetail/" + noticeId,
                "/popup/noticePopupSave?type=add",
                "/popup/noticePopupSave?type=modify&id=" + noticeId}) {
            assertPopupFragment(path, get(path, true));
        }

        assertThat(get("/popup/noticePopupDetail/" + noticeId, true).body()).contains(title);
    }

    @Test
    @Order(11)
    @DisplayName("사이드바 : 권한 계층 적용으로 ADMIN 은 모든 메뉴가 보인다")
    void sidebarMenusForAdmin() throws Exception {
        String body = get("/", false).body();

        for (String path : new String[]{"/userManage", "/projectManage", "/systemLog", "/notice",
                "/dataDictionary", "/tableManage", "/columnManage",
                "/codeRuleManage", "/codeRule", "/designManage", "/testManage"}) {
            assertThat(body).as(path).contains("href=\"" + path + "\"");
        }
    }

    @Test
    @Order(12)
    @DisplayName("폰트는 브라우저 캐시 허용 (페이지 이동마다 재다운로드 → 폰트 깜빡임 방지)")
    void fontCacheable() throws Exception {
        HttpResponse<String> font = get("/fonts/pretendard/PretendardVariable.woff2", false);

        assertThat(font.statusCode()).isEqualTo(200);
        String cacheControl = font.headers().firstValue("Cache-Control").orElse("");
        assertThat(cacheControl).as("Cache-Control").contains("max-age=31536000").doesNotContain("no-store");

        // CSS 는 캐시하되 매번 변경 여부를 확인한다 (배포 후 바로 반영)
        HttpResponse<String> css = get("/css/primer.css", false);
        assertThat(css.statusCode()).isEqualTo(200);
        assertThat(css.headers().firstValue("Cache-Control").orElse("")).as("CSS Cache-Control")
                .contains("no-cache").doesNotContain("no-store");

        // 화면은 기존처럼 캐시하지 않는다 (로그인 사용자 정보 포함)
        assertThat(get("/", false).headers().firstValue("Cache-Control").orElse("")).contains("no-store");
    }

    @ParameterizedTest(name = "그리드 {0}")
    @ValueSource(strings = {
            "/api/getStandardWords", "/api/getStandardTerms", "/api/getStandardDomains",
            "/api/selectTable", "/api/selectColumn", "/api/selectCodeRule", "/api/selectCodeRuleManage",
            "/api/selectNotice", "/api/selectUser", "/api/getSystemLog", "/api/getProject/" + PROJECT_ID
    })
    @Order(20)
    @DisplayName("그리드 API (column / data)")
    void gridApis(String dataUrl) throws Exception {
        HttpResponse<String> column = get(dataUrl + "/column", true);
        assertThat(column.statusCode()).as(dataUrl + "/column").isEqualTo(200);
        assertThat(column.body()).as(dataUrl + "/column").startsWith("[");

        HttpResponse<String> data = get(dataUrl + "/data?page=0&size=10", true);
        assertThat(data.statusCode()).as(dataUrl + "/data").isEqualTo(200);
        assertThat(data.body()).as(dataUrl + "/data").contains("\"recordsTotal\"");
    }

    @ParameterizedTest(name = "API {0}")
    @ValueSource(strings = {"/isApprovalAvailable", "/api/activeUsers", "/api/selectCodeRuleManage/template", "/api/commonColumn", "/v3/api-docs"})
    @Order(21)
    @DisplayName("기타 주요 API")
    void apis(String path) throws Exception {
        assertThat(get(path, true).statusCode()).as(path).isEqualTo(200);
    }

    @Test
    @Order(22)
    @DisplayName("표준 단어 기준 테이블명 생성 API : 단어별 상태와 valid 를 JSON 으로 반환")
    void generateTableName() throws Exception {
        String korName = java.net.URLEncoder.encode("없는단어_테스트", java.nio.charset.StandardCharsets.UTF_8);
        HttpResponse<String> response = get("/api/selectTable/tableName?tableKorName=" + korName, true);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("\"valid\":false")
                .contains("\"tableKorName\":\"없는단어 테스트\"")
                .contains("\"status\":\"NOT_FOUND\"");
    }

    @Test
    @Order(1)
    @DisplayName("JPA : 지연 로딩 연관을 묶어 조회하도록 default_batch_fetch_size(100) 가 적용된다")
    void batchFetchSize() {
        assertThat(entityManagerFactory.getProperties().get("hibernate.default_batch_fetch_size"))
                .hasToString("100");
    }

    @Test
    @Order(24)
    @DisplayName("데이터사전 엑셀 출력 : 페이지 단위 조회 + 스트리밍 쓰기로 등록한 용어(도메인/약어 지연 로딩 포함)가 들어간다")
    void exportDictionary() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase().replaceAll("[^A-Z]", "X");
        String category = "스모크분류" + suffix;
        String domainName = "스모크도메인" + suffix;

        assertThat(postJson("/api/insertDataDictionary/standardDomains", """
                {"revisionNumber":1,"commonStandardDomainGroupName":"문자","commonStandardDomainCategory":"%s",
                 "commonStandardDomainName":"%s","commonStandardDomainDescription":"스모크","dataType":"VARCHAR",
                 "dataLength":100,"dataDecimalLength":0,"storageFormat":"","displayFormat":"","unit":"","allowedValues":""}
                """.formatted(category, domainName)).statusCode()).isEqualTo(200);
        assertThat(postJson("/api/insertDataDictionary/standardWords", """
                {"revisionNumber":1,"commonStandardWordName":"스모크%s","commonStandardWordAbbreviation":"SMK%s",
                 "commonStandardWordEnglishName":"Smoke","commonStandardWordDescription":"스모크","isFormatWord":"N",
                 "commonStandardDomainCategory":"-","synonyms":"","restrictedWords":""}
                """.formatted(suffix, suffix)).statusCode()).isEqualTo(200);
        assertThat(postJson("/api/insertDataDictionary/standardWords", """
                {"revisionNumber":1,"commonStandardWordName":"명칭%s","commonStandardWordAbbreviation":"NM%s",
                 "commonStandardWordEnglishName":"Name","commonStandardWordDescription":"스모크","isFormatWord":"N",
                 "commonStandardDomainCategory":"%s","synonyms":"","restrictedWords":""}
                """.formatted(suffix, suffix, category)).statusCode()).isEqualTo(200);
        assertThat(postJson("/api/insertDataDictionary/standardTerms", """
                {"revisionNumber":1,"commonStandardTermName":"스모크%s_명칭%s","commonStandardTermDescription":"스모크",
                 "commonStandardTermAbbreviation":"SMK%s_NM%s","commonStandardDomainName":"%s","synonyms":""}
                """.formatted(suffix, suffix, suffix, suffix, domainName)).statusCode()).isEqualTo(200);

        HttpResponse<byte[]> response = getBytes("/api/downloadTemplate/dictionary");
        assertThat(response.statusCode()).isEqualTo(200);

        List<Map<String, String>> terms = new java.util.ArrayList<>();
        com.koboolean.metagen.common.utils.ExcelUtils.readExcelRows(
                new org.springframework.mock.web.MockMultipartFile("file", "dictionary.xlsx", null, response.body()),
                0, List.of("id", "revision", "termName", "description", "abbreviation", "domainName"), 1, terms::add);

        assertThat(terms).anySatisfy(row -> {
            assertThat(row.get("termName")).isEqualTo("스모크" + suffix + "_명칭" + suffix);
            assertThat(row.get("abbreviation")).isEqualTo("SMK" + suffix + "_NM" + suffix);
            assertThat(row.get("domainName")).isEqualTo(domainName);
        });
    }

    @Test
    @Order(24)
    @DisplayName("설계서 엑셀 출력(전체) : 페이지 단위 조회로 정상 xlsx 를 내려준다")
    void exportDesignAll() throws Exception {
        HttpResponse<byte[]> response = client.send(HttpRequest.newBuilder(uri("/api/printDesign"))
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("X-XSRF-TOKEN", csrfToken())
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"formatType\":\"functionSpec\",\"formatText\":\"기능 정의서\",\"type\":\"all\",\"column\":\"functionGroup\",\"searchValue\":\"\"}",
                        StandardCharsets.UTF_8))
                .build(), HttpResponse.BodyHandlers.ofByteArray());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(new String(response.body(), 0, 2, StandardCharsets.US_ASCII)).as("xlsx(zip) 시그니처").isEqualTo("PK");
    }

    @Test
    @Order(23)
    @DisplayName("컬럼명 중복 확인 API : 없는 컬럼명이면 duplicated=false")
    void checkColumnDuplicate() throws Exception {
        HttpResponse<String> response = get("/api/selectColumn/duplicate?tableInfoId=0&columnName=NOT_EXISTS_COLUMN", true);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"duplicated\":false");
    }

    @Test
    @Order(23)
    @DisplayName("컬럼 일괄 저장 API : JSON 요청을 받고, 없는 테이블이면 TABLE_IS_NOT_DEFINED(400)")
    void saveColumnsBulkUnknownTable() throws Exception {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri("/api/saveColumn/bulk"))
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("X-XSRF-TOKEN", csrfToken())
                .PUT(HttpRequest.BodyPublishers.ofString(
                        "{\"tableInfoId\":-1,\"rows\":[{\"key\":\"new-1\",\"termId\":-1,\"columnDesc\":\"설명\",\"dataType\":\"VARCHAR\",\"isPk\":\"N\"}],\"deletedIds\":[]}",
                        StandardCharsets.UTF_8))
                .build(), HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("TABLE_IS_NOT_DEFINED");
    }

    @Test
    @Order(23)
    @DisplayName("표준용어 일괄 찾기 API : 붙여넣은 줄마다 상태를 돌려준다 (없는 용어는 NOT_FOUND)")
    void resolveTerms() throws Exception {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri("/api/selectColumn/term/resolve"))
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("X-XSRF-TOKEN", csrfToken())
                .POST(HttpRequest.BodyPublishers.ofString("[\"없는 용어 테스트\",\"NOT_EXISTS_ABBR\",\" \"]", StandardCharsets.UTF_8))
                .build(), HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("\"input\":\"없는 용어 테스트\"")
                .contains("\"input\":\"NOT_EXISTS_ABBR\"")
                .contains("\"status\":\"NOT_FOUND\"")
                .doesNotContain("\"input\":\" \"");
    }

    @Test
    @Order(23)
    @DisplayName("표준용어 중복 확인 API : 없는 약어/도메인이면 duplicated=false")
    void checkStandardTermDuplicate() throws Exception {
        String domain = java.net.URLEncoder.encode("없는도메인", java.nio.charset.StandardCharsets.UTF_8);
        HttpResponse<String> response = get("/api/getStandardTerms/duplicate?abbreviation=NOT_EXISTS_ABBR&domainName=" + domain, true);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"duplicated\":false");
    }

    @Test
    @Order(30)
    @DisplayName("API 호출 시 시스템 로그 저장 (민감정보 마스킹 포함)")
    void systemLogSaved() throws Exception {
        long before = countLogs("/api/updateName");

        HttpResponse<String> response = postJson("/api/updateName", "{\"name\":\"관리자\"}");
        assertThat(response.statusCode()).isEqualTo(200);

        // afterCompletion에서 저장되므로 응답 직후 약간의 지연이 있을 수 있다
        long deadline = System.currentTimeMillis() + 5_000;
        while (countLogs("/api/updateName") <= before && System.currentTimeMillis() < deadline) {
            Thread.sleep(100);
        }
        assertThat(countLogs("/api/updateName")).isGreaterThan(before);

        // LogsConfig 제외 패턴(/api/**/data, /api/**/column) : 앞 단계에서 호출한 그리드 API는 저장되지 않아야 한다
        assertThat(logsRepository.findAll())
                .extracting(Logs::getLogUrl)
                .isNotEmpty()
                .noneMatch(url -> url.endsWith("/data") || url.endsWith("/column"));

        // 로그인 프로젝트 조회 요청은 비밀번호가 포함되므로 저장되더라도 원문이 남지 않아야 한다
        assertThat(logsRepository.findAll())
                .extracting(Logs::getRequestBody)
                .noneMatch(body -> body != null && (body.contains("\"password\":\"" + PASSWORD + "\"")
                        || body.contains(PASSWORD))); // 로그인 전 비밀번호 변경의 newPassword 포함
    }

    @Test
    @Order(40)
    @DisplayName("채팅 웹소켓 : 본인 채널 구독 → 메시지 전송/수신 → 이력 조회")
    void chatWebSocket() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new SimpleMessageConverter());

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.add("Cookie", "JSESSIONID=" + cookie(cookieManager, "JSESSIONID"));

        // SockJS 엔드포인트의 raw WebSocket 경로로 접속
        StompSession session = stompClient.connectAsync("ws://localhost:" + port + "/ws-chat/websocket",
                handshakeHeaders, new StompHeaders(), new StompSessionHandlerAdapter() {
                }).get(10, TimeUnit.SECONDS);

        try {
            BlockingQueue<String> received = new ArrayBlockingQueue<>(1);
            session.subscribe("/sub/chat/" + loginUserId, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return byte[].class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    received.offer(new String((byte[]) payload, StandardCharsets.UTF_8));
                }
            });

            String content = "smoke-" + UUID.randomUUID();
            StompHeaders sendHeaders = new StompHeaders();
            sendHeaders.setDestination("/pub/chat.send");
            sendHeaders.setContentType(org.springframework.util.MimeTypeUtils.APPLICATION_JSON);

            // 구독 처리 후 전송 (SUBSCRIBE는 비동기로 처리된다)
            Thread.sleep(300);
            session.send(sendHeaders, ("{\"to\":\"" + loginUserId + "\",\"content\":\"" + content + "\"}")
                    .getBytes(StandardCharsets.UTF_8));

            String message = received.poll(10, TimeUnit.SECONDS);
            assertThat(message).isNotNull().contains(content).contains("\"from\":\"" + loginUserId + "\"");

            HttpResponse<String> history = get("/api/chat/history?user="
                    + URLEncoder.encode(loginUserId, StandardCharsets.UTF_8), true);
            assertThat(history.statusCode()).isEqualTo(200);
            assertThat(history.body()).contains(content);
        } finally {
            session.disconnect();
            stompClient.stop();
        }
    }

    // ===== helpers =====

    private long countLogs(String url) {
        return logsRepository.findAll().stream().filter(log -> url.equals(log.getLogUrl())).count();
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private HttpResponse<String> get(String path, boolean ajax) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path)).GET();
        if (ajax) {
            builder.header("X-Requested-With", "XMLHttpRequest");
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<byte[]> getBytes(String path) throws IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder(uri(path)).header("X-Requested-With", "XMLHttpRequest").GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
    }

    private HttpResponse<String> postJson(String path, String json) throws IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("X-XSRF-TOKEN", csrfToken())
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postForm(String path, String form) throws IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form + "&_csrf=" + csrfToken()))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private String csrfToken() {
        return cookie(cookieManager, "XSRF-TOKEN");
    }

    private static String cookie(CookieManager manager, String name) {
        return manager.getCookieStore().getCookies().stream()
                .filter(c -> name.equals(c.getName()))
                .reduce((first, second) -> second) // 갱신된 경우 마지막 값을 사용
                .map(HttpCookie::getValue)
                .orElse(null);
    }

    private static String location(HttpResponse<?> response) {
        return response.headers().firstValue("Location").orElse("");
    }
}
