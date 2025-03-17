package com.koboolean.metagen.security.provider;

import com.koboolean.metagen.security.domain.dto.AccountContext;
import com.koboolean.metagen.security.details.FormWebAuthenticationDetails;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.exception.SecretException;
import com.koboolean.metagen.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("authenticationProvider")
@RequiredArgsConstructor
public class FormAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Value("${secret.key}")
    private String secretYmlKey;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String loginId = authentication.getName();
        String password = (String) authentication.getCredentials();

        AccountContext accountContext = (AccountContext)userDetailsService.loadUserByUsername(loginId);

        if (!passwordEncoder.matches(password, accountContext.getPassword())) {
            throw new BadCredentialsException("패스워드가 일치하지 않습니다.");
        }

        FormWebAuthenticationDetails details = (FormWebAuthenticationDetails) authentication.getDetails();
        String secretKey = details.getSecretKey();
        if (secretKey == null || !secretKey.equals(secretYmlKey)) {
            throw new SecretException("시크릿 키가 일치하지 않습니다.");
        }

        // Project ID 저장
        AccountDto accountDto = accountContext.getAccountDto();

        accountDto.setProjectId(details.getProjectId());

        List<Object> authorities = (ArrayList) accountContext.getAuthorities();

        accountDto.setRoleName(userService.getRoleName(authorities.get(0).toString()));
        accountDto.setPassword(null);

        return new UsernamePasswordAuthenticationToken(accountDto, null, accountContext.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
