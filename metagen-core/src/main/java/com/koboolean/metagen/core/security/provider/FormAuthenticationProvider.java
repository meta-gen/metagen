package com.koboolean.metagen.core.security.provider;

import com.koboolean.metagen.core.security.domain.dto.AccountContext;
import com.koboolean.metagen.core.security.details.FormWebAuthenticationDetails;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.common.exception.ProjectAccessException;
import com.koboolean.metagen.core.login.service.LoginUserCache;
import com.koboolean.metagen.core.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
    private final LoginUserCache loginUserCache;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String loginId = authentication.getName();
        String password = (String) authentication.getCredentials();

        AccountContext accountContext = (AccountContext)userDetailsService.loadUserByUsername(loginId);

        if (!passwordEncoder.matches(password, accountContext.getPassword())) {
            throw new BadCredentialsException("패스워드가 일치하지 않습니다.");
        }

        // 선택한 프로젝트가 접근 가능한 프로젝트인지 확인 (요청 값을 그대로 신뢰하지 않음)
        FormWebAuthenticationDetails details = (FormWebAuthenticationDetails) authentication.getDetails();
        Long projectId = details.getProjectId();

        boolean isAccessible = projectId != null && userService.selectProject(loginId).stream()
                .anyMatch(project -> projectId.equals(project.getId()));

        if (!isAccessible) {
            throw new ProjectAccessException("선택한 프로젝트에 접근할 수 없습니다.");
        }

        // Project ID 저장
        AccountDto accountDto = accountContext.getAccountDto();

        accountDto.setProjectId(projectId);

        List<Object> authorities = (ArrayList) accountContext.getAuthorities();

        accountDto.setRoleName(userService.getRoleName(authorities.get(0).toString()));
        accountDto.setPassword(null);

        loginUserCache.save(accountDto);

        HttpServletRequest request = details.getRequest();
        if (request != null) {
            request.getSession().setAttribute("account", accountDto);
        }

        return new UsernamePasswordAuthenticationToken(accountDto, null, accountContext.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
