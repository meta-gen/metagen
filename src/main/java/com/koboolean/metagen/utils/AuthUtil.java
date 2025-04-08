package com.koboolean.metagen.utils;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.project.service.ProjectManageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private static final Logger log = LoggerFactory.getLogger(AuthUtil.class);
    private static ProjectManageService projectManageService;

    @Autowired
    public AuthUtil(ProjectManageService projectManageService) {
        AuthUtil.projectManageService = projectManageService;
    }

    public static boolean isApprovalAvailable() {
        // 디버깅 로그 추가
        AccountDto accountDto = (AccountDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isLoginManageProject = projectManageService.selectLoginProject(accountDto.getProjectId(), accountDto.getId());

        boolean isAuthorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .peek(log::debug) // 디버깅 로그 추가
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER"));

        return isLoginManageProject || isAuthorities;
    }

    public static boolean isApprovalAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .peek(log::debug) // 디버깅 로그 추가
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }


}
