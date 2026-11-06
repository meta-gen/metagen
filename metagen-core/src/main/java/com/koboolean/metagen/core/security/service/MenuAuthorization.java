package com.koboolean.metagen.core.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 사이드바 메뉴 노출 권한
 * <p>
 * 접근할 수 없는 화면은 메뉴에서 아예 보이지 않도록, 화면별 최소 권한을 한 곳에서 관리한다.
 * 최소 권한은 "화면 진입 권한"과 "화면이 호출하는 그리드 API 권한" 중 높은 쪽이다.
 * (예: 컬럼 관리 화면은 VIEWER 이지만 컬럼 API 가 USER 이므로 USER)
 * <p>
 * 권한 계층(RoleHierarchyConfig)을 적용하므로 상위 권한은 하위 권한 메뉴를 모두 볼 수 있다.
 * Thymeleaf 에서 {@code th:if="${@menuAuth.canView('user-manage')}"} 형태로 사용한다.
 */
@Component("menuAuth")
@RequiredArgsConstructor
public class MenuAuthorization {

    /** 메뉴 ID(사이드바 data-menu-id / data-submenu-id) → 최소 권한 */
    public static final Map<String, String> MENU_ROLES = Map.ofEntries(
            Map.entry("dashboard", "NOT_APPROVE"),

            // 시스템 관리
            Map.entry("user-manage", "ADMIN"),
            Map.entry("project-manage", "DBA"),
            Map.entry("system-log", "DBA"),

            // 운영 관리
            Map.entry("notice", "NOT_APPROVE"),

            // 데이터 관리
            Map.entry("data-dictionary", "VIEWER"),
            Map.entry("table-manage", "VIEWER"),
            Map.entry("column-manage", "USER"),

            // 설계기반 관리
            Map.entry("code-rule-manage", "VIEWER"),
            Map.entry("code-rule", "USER"),
            Map.entry("design-manage", "USER"),
            Map.entry("test-manage", "USER")
    );

    /** 1뎁스 메뉴 그룹 → 하위 메뉴 ID (하위 메뉴가 하나라도 보이면 그룹을 노출) */
    public static final Map<String, String[]> MENU_GROUPS = Map.of(
            "system-management", new String[]{"user-manage", "project-manage", "system-log"},
            "operation-management", new String[]{"notice"},
            "data-management", new String[]{"data-dictionary", "table-manage", "column-manage"},
            "design-test", new String[]{"code-rule-manage", "code-rule", "design-manage", "test-manage"}
    );

    private final RoleHierarchy roleHierarchy;

    /**
     * 메뉴(또는 메뉴 그룹)를 현재 사용자에게 보여줄지 여부
     */
    public boolean canView(String menuId) {
        String[] children = MENU_GROUPS.get(menuId);
        if (children != null) {
            for (String child : children) {
                if (canView(child)) return true;
            }
            return false;
        }

        String role = MENU_ROLES.get(menuId);
        return role != null && hasRole(role);
    }

    /**
     * 권한 계층을 적용하여 현재 사용자가 해당 권한을 가졌는지 확인한다.
     *
     * @param role ROLE_ 접두사를 뺀 권한명 (예: USER)
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;

        String target = "ROLE_" + role;
        return roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities()).stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(target::equals);
    }
}
