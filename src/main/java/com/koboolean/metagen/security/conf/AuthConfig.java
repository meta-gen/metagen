package com.koboolean.metagen.security.conf;

import com.koboolean.metagen.security.manager.CustomAuthorizationManager;
import com.koboolean.metagen.security.service.RoleHierarchyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@RequiredArgsConstructor
public class AuthConfig {

    private final CustomAuthorizationManager authorizationManager;
    private final RoleHierarchyConfig roleHierarchyConfig;
    private final RoleHierarchyImpl roleHierarchyImpl;

    private static final Logger logger = LoggerFactory.getLogger(AuthConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // 서버 로딩이 완료된 이후 실행
        authorizationManager.reload();
        roleHierarchyConfig.updateHierarchy(roleHierarchyImpl);
        logger.info("Application의 ROLE을 재반영 했습니다.");
    }

}
