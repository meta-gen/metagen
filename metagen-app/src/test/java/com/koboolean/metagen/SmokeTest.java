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
    private static final String PASSWORD = "admin";
    private static final long PROJECT_ID = 0L;

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private LogsRepository logsRepository;

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

        // 2. 로그인 가능 프로젝트 조회
        HttpResponse<String> projects = postJson("/api/login/projects",
                "{\"username\":\"" + USERNAME + "\",\"password\":\"" + PASSWORD + "\"}");
        assertThat(projects.statusCode()).isEqualTo(200);
        assertThat(projects.body()).contains("\"projectName\":\"MAIN\"");

        // 3. 폼 로그인 (선택한 프로젝트 포함) : 초기 계정은 비밀번호 변경 전이므로 /account로 이동
        HttpResponse<String> login = postForm("/login",
                "username=" + USERNAME + "&password=" + PASSWORD + "&project_id=" + PROJECT_ID);
        assertThat(login.statusCode()).isEqualTo(302);
        assertThat(location(login)).endsWith("/account");

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

        other.send(HttpRequest.newBuilder(uri("/login")).GET().build(), HttpResponse.BodyHandlers.discarding());
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
            "/userManage", "/systemLog", "/projectManage", "/help"
    })
    @Order(10)
    @DisplayName("주요 화면 렌더링")
    void pages(String path) throws Exception {
        HttpResponse<String> response = get(path, false);

        assertThat(response.statusCode()).as(path).isEqualTo(200);
        assertThat(response.body()).as(path).contains("</html>");
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
    @ValueSource(strings = {"/isApprovalAvailable", "/api/activeUsers", "/api/selectCodeRuleManage/template", "/v3/api-docs"})
    @Order(21)
    @DisplayName("기타 주요 API")
    void apis(String path) throws Exception {
        assertThat(get(path, true).statusCode()).as(path).isEqualTo(200);
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
                .noneMatch(body -> body != null && body.contains("\"password\":\"" + PASSWORD + "\""));
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
