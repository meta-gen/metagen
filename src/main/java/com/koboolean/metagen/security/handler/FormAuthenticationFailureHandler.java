package com.koboolean.metagen.security.handler;

import com.koboolean.metagen.security.exception.SecretException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component("failureHandler")
public class FormAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "아이디 또는 비밀번호가 잘못되었습니다.";

        if (exception instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 잘못되었습니다.";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "사용자를 찾을 수 없습니다.";
        } else if (exception instanceof CredentialsExpiredException) {
            errorMessage = "비밀번호가 만료되었습니다.";
        } else if (exception instanceof SecretException) {
            errorMessage = "비밀 키가 잘못되었습니다.";
        }

        setDefaultFailureUrl("/login?error=true&exception=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));

        super.onAuthenticationFailure(request, response, exception);
    }
}
