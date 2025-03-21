package com.koboolean.metagen.security.controller;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.utils.AuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
public class SecurityRestController {

    private final Logger log = LoggerFactory.getLogger(SecurityRestController.class);

    @GetMapping(value = "/isApprovalAvailable")
    public ResponseEntity<Map<String, Boolean>> isApprovalAvailable() {
        boolean isApprovalAvailable = AuthUtil.isIsApprovalAvailable();

        return ResponseEntity.ok(Map.of("result", isApprovalAvailable));
    }


}
