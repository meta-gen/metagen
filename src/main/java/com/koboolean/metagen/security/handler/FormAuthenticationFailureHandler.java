package com.koboolean.metagen.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.koboolean.metagen.security.exception.ProjectAccessException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component("failureHandler")
public class FormAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception) throws IOException, ServletException {

        // 아이디 없음 / 비밀번호 불일치는 아이디 존재 여부가 드러나지 않도록 같은 메시지 사용
        String errorMessage = "아이디 또는 비밀번호가 잘못되었습니다.";

        if (exception instanceof CredentialsExpiredException) {
            errorMessage = "비밀번호가 만료되었습니다.";
        } else if (exception instanceof ProjectAccessException) {
            errorMessage = exception.getMessage();
        }

        // 공유(singleton) 핸들러이므로 setDefaultFailureUrl로 상태를 바꾸지 않고 요청마다 직접 redirect
        saveException(request, exception);
        getRedirectStrategy().sendRedirect(request, response,
                "/login?error=true&exception=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
    }
}
