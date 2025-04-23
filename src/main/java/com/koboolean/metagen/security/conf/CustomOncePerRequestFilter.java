package com.koboolean.metagen.security.conf;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CustomOncePerRequestFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // CSRF 토큰 갱신 (기존 로직 유지)
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
            String token = csrfToken.getToken();
            if (cookie == null || (token != null && !token.equals(cookie.getValue()))) {
                updateSessionCookie(response, "XSRF-TOKEN", token, false);
            }
        }

        // Only wrap if not already wrapped
        HttpServletRequest requestToUse = request;
        HttpServletResponse responseToUse = response;

        if (!(requestToUse instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request);
        }

        if (!(responseToUse instanceof ContentCachingResponseWrapper)) {
            responseToUse = new ContentCachingResponseWrapper(response);
        }

        // 요청 본문을 강제적으로 캐싱하지 않고 그대로 유지
        request.setAttribute("CACHED_REQUEST", requestToUse);
        request.setAttribute("CACHED_RESPONSE", responseToUse);

        AccountDto accountDto = (AccountDto) request.getSession().getAttribute("account");

        if (accountDto != null && accountDto.getId() != null) {
            String key = "login:user:" + accountDto.getId();
            Boolean exists = redisTemplate.hasKey(key);
            if (exists) {
                // TTL 연장
                redisTemplate.expire(key, Duration.ofMinutes(30));
            }
        }

        // 필터 체인 실행 (중요: 감싼 요청과 응답을 사용해야 함)
        filterChain.doFilter(requestToUse, responseToUse);

        // 응답을 클라이언트로 다시 전달
        if (responseToUse instanceof ContentCachingResponseWrapper) {
            ((ContentCachingResponseWrapper) responseToUse).copyBodyToResponse();
        }
    }

    private static void updateSessionCookie(HttpServletResponse response, String type, String token, boolean httpOnly) {
        Cookie cookie = new Cookie(type, token);
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(1800); // 요청 시마다 만료 시간 갱신
        response.addCookie(cookie);
    }
}
