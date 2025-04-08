package com.koboolean.metagen.security.controller;

import com.koboolean.metagen.security.conf.RoleHierarchyConfig;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.security.manager.CustomAuthorizationManager;
import com.koboolean.metagen.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SecurityRestController {

    private final CustomAuthorizationManager authorizationManager;
    private final RoleHierarchyConfig roleHierarchyConfig;
    private final RoleHierarchyImpl roleHierarchyImpl;

    @GetMapping(value = "/api/updateRole")
    public ResponseEntity<Map<String, Boolean>> updateRole() {
        if(!AuthUtil.isApprovalAdmin()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }
        
        // 권한 Reload
        authorizationManager.reload();
        // 권한 계층 변경
        roleHierarchyConfig.updateHierarchy(roleHierarchyImpl);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @GetMapping(value = "/isApprovalAvailable")
    public ResponseEntity<Map<String, Boolean>> isApprovalAvailable() {
        boolean isApprovalAvailable = AuthUtil.isApprovalAvailable();

        return ResponseEntity.ok(Map.of("result", isApprovalAvailable));
    }


}
