package com.koboolean.metagen.core.security.controller;

import com.koboolean.metagen.core.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SecurityRestController {

    @GetMapping(value = "/isApprovalAvailable")
    public ResponseEntity<Map<String, Boolean>> isApprovalAvailable() {
        boolean isApprovalAvailable = AuthUtil.isApprovalAvailable();

        return ResponseEntity.ok(Map.of("result", isApprovalAvailable));
    }


}
