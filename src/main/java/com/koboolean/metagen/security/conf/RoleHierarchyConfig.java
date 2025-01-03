package com.koboolean.metagen.security.conf;

import com.koboolean.metagen.security.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Configuration
@RequiredArgsConstructor
public class RoleHierarchyConfig {

    private final RoleService roleService;

    @Bean
    public RoleHierarchyImpl roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(findHierarchy());
        return roleHierarchy;
    }

    private String findHierarchy() {
        return roleService.findAllHierarchy();
    }

    // 동적으로 RoleHierarchy를 업데이트하는 메서드
    public String updateHierarchy(RoleHierarchyImpl roleHierarchy) {
        String hierarchy = findHierarchy();
        roleHierarchy.setHierarchy(hierarchy);
        return hierarchy;
    }
}
