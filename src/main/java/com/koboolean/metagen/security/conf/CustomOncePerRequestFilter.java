package com.koboolean.metagen.security.conf;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Component
public class CustomOncePerRequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        // CSRF토큰 갱신
        if (csrfToken != null) {
            Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
            String token = csrfToken.getToken();
            if (cookie == null || (token != null && !token.equals(cookie.getValue()))) {
                updateSessionCookie(response, "XSRF-TOKEN", token, false);
            }
        }

        /**
         * TODO: 필요 시 JSESSIONID 갱신 로직 활성화 고려
         * JSESSIONID 갱신 - 요청마다 JSESSIONID를 갱신하여 세션 만료 시간을 연장할 수 있음.
         * 하지만, 서버 부하(stress)가 증가할 가능성이 있으므로 현재는 비활성화 처리.
         * 필요 시 아래 코드를 활성화하여 사용 가능.
         * 활성화 시 기본 default max-age 30분을 위해 application-local.yml 내 max-age: 1800 주석 해제 필요
         */
        // if (request.getSession(false) != null) {
        //     String sessionId = request.getSession().getId();
        //     updateSessionCookie(response, "JSESSIONID", sessionId, true);
        // }

        filterChain.doFilter(request, response);
    }

    private static void updateSessionCookie(HttpServletResponse response,String type, String token, boolean httpOnly) {
        Cookie cookie = new Cookie("XSRF-TOKEN", token);
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(1800); // 요청 시마다 만료 시간 갱신
        response.addCookie(cookie);
    }
}
