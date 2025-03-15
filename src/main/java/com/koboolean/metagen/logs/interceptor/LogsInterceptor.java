package com.koboolean.metagen.logs.interceptor;

import com.koboolean.metagen.logs.domain.entity.Logs;
import com.koboolean.metagen.logs.service.LogsService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;

/**
 * Restful API 형식으로 들어오는 HTTP 요청단위별 동작을 위해 Interceptor에 로깅 기능 추가
 * - HTTP 요청(Request) 및 응답(Response) 로깅에 Interceptor 적용
 * - 모든 메서드 기반 로깅이 필요할 경우 AOP를 사용하여 적용
 */
@RequiredArgsConstructor
public class LogsInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LogsInterceptor.class);
    private final LogsService logsService;

    // 요청 정보를 저장할 ThreadLocal 변수 (각 요청마다 독립적인 데이터 저장)
    private final ThreadLocal<RequestDetails> requestDetailsThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 필터에서 저장한 요청 및 응답 객체 가져오기
        ContentCachingRequestWrapper wrappedRequest = (ContentCachingRequestWrapper) request.getAttribute("CACHED_REQUEST");
        ContentCachingResponseWrapper wrappedResponse = (ContentCachingResponseWrapper) request.getAttribute("CACHED_RESPONSE");

        if (wrappedRequest == null || wrappedResponse == null) {
            logger.error("wrappedRequest or wrappedResponse is NULL. 필터가 적용되지 않음.");
            return true;
        }

        // 요청 정보를 저장할 객체 생성 및 저장
        RequestDetails details = new RequestDetails();
        details.logUrl = request.getRequestURI();
        details.method = request.getMethod();
        details.ip = request.getRemoteAddr();
        details.userAgent = request.getHeader("User-Agent");
        details.wrappedRequest = wrappedRequest;
        details.wrappedResponse = wrappedResponse;

        // ThreadLocal에 저장하여 이후 후처리에서 사용
        requestDetailsThreadLocal.set(details);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // 필요 시 추가 처리 가능
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Logs log = null;

        try {
            // preHandle에서 저장된 요청 정보를 가져옴
            RequestDetails details = requestDetailsThreadLocal.get();
            if (details == null || details.wrappedRequest == null || details.wrappedResponse == null) {
                logger.error("Request details not available in afterCompletion");
                return;
            }

            // 요청 및 응답 본문 가져오기
            String requestBody = getRequestBody(details.wrappedRequest);
            logger.debug("Captured Request Body: " + requestBody);

            String header = response.getHeader("Content-Disposition");

            // fileData 데이터 저장 시 크기 문제로 인한 File Data 출력
            String responseBody = header != null && (header.contains("attachment") || header.contains("filename=")) ? "File Data" : getResponseBody(details.wrappedResponse);
            logger.debug("Captured Response Body: " + responseBody);

            int statusCode = response.getStatus(); // 응답 상태 코드

            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof AccountDto) {
                AccountDto accountDto = (AccountDto) authentication.getPrincipal();

                // 로그 객체 생성 및 저장
                log = Logs.builder()
                        .logUrl(details.logUrl)
                        .method(details.method)
                        .ip(details.ip)
                        .username(accountDto.getUsername())
                        .roleName(accountDto.getRoleName())
                        .statusCode(statusCode)
                        .requestBody(requestBody) // 요청 본문 저장
                        .responseBody(responseBody) // 응답 본문 저장
                        .userAgent(details.userAgent)
                        .build();

                log.setTimestamp();

                logger.debug("Saved Log Data: " + log);
                logsService.saveLogs(log);
            }
        } catch (Exception e) {
            logger.error("Error saving request log", e);
        } finally {
            // ThreadLocal 데이터 제거 (메모리 누수 방지)
            requestDetailsThreadLocal.remove();

            if (ex != null) {
                logger.error("Exception Occurred: ", ex);

                if (log != null) {
                    log.setErrorMessage(ex.getMessage());
                }
            }
        }
    }

    /**
     * 요청 본문을 가져오는 메서드
     * ContentCachingRequestWrapper에서 저장된 요청 데이터를 읽어옴
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        return (buf.length > 0) ? new String(buf, StandardCharsets.UTF_8) : "N/A";
    }

    /**
     * 응답 본문을 가져오는 메서드
     * ContentCachingResponseWrapper에서 저장된 응답 데이터를 읽어옴
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] buf = response.getContentAsByteArray();
        return (buf.length > 0) ? new String(buf, StandardCharsets.UTF_8) : "N/A";
    }

    /**
     * 요청 정보를 저장할 내부 클래스
     */
    private static class RequestDetails {
        String logUrl;
        String method;
        String ip;
        String userAgent;
        ContentCachingRequestWrapper wrappedRequest;
        ContentCachingResponseWrapper wrappedResponse;
    }
}
