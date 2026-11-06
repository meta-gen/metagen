package com.koboolean.metagen.security.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Configuration
public class RoleHierarchyConfig {

    /**
     * 권한 계층
     * ADMIN > MANAGER > DBA > USER > VIEWER > NOT_APPROVE > ANONYMOUS
     *                 > USER
     */
    @Bean
    public static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
                ROLE_ADMIN > ROLE_MANAGER
                ROLE_MANAGER > ROLE_DBA
                ROLE_MANAGER > ROLE_USER
                ROLE_DBA > ROLE_USER
                ROLE_USER > ROLE_VIEWER
                ROLE_VIEWER > ROLE_NOT_APPROVE
                ROLE_NOT_APPROVE > ROLE_ANONYMOUS
                """);
    }
}
