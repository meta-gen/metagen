package com.koboolean.metagen.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {

    private static final Logger log = LoggerFactory.getLogger(AuthUtil.class);

    public static boolean isIsApprovalAvailable() {
        // 디버깅 로그 추가
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .peek(log::debug) // 디버깅 로그 추가
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER"));
    }
}
