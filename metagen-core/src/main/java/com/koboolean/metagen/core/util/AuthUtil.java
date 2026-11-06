package com.koboolean.metagen.core.util;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private static final Logger log = LoggerFactory.getLogger(AuthUtil.class);
    private static ProjectRepository projectRepository;

    @Autowired
    public AuthUtil(ProjectRepository projectRepository) {
        AuthUtil.projectRepository = projectRepository;
    }

    public static boolean isApprovalAvailable() {
        // 디버깅 로그 추가
        AccountDto accountDto = (AccountDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isLoginManageProject = projectRepository.findById(accountDto.getProjectId())
                .map(project -> project.getAccount().getId().equals(Long.parseLong(accountDto.getId())))
                .orElse(false);

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

    public static boolean isValidPassword(String password) {
        String pattern = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$";
        return Pattern.matches(pattern, password);
    }


}
