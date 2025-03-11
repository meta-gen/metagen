package com.koboolean.metagen.security.controller;


import com.koboolean.metagen.security.conf.RoleHierarchyConfig;
import com.koboolean.metagen.security.manager.CustomAuthorizationManager;
import com.koboolean.metagen.security.repository.RoleRepository;
import com.koboolean.metagen.security.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
public class SecurityController {

    private final CustomAuthorizationManager authorizationManager;
    private final RoleHierarchyConfig roleHierarchyConfig;
    private final RoleHierarchyImpl roleHierarchyImpl;

    @GetMapping(value = "/updateRole")
    public String updateRole() {

        // 권한 Reload
        authorizationManager.reload();
        // 권한 계층 변경
        roleHierarchyConfig.updateHierarchy(roleHierarchyImpl);

        return "redirect:/";

    }
}
