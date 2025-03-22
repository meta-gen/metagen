package com.koboolean.metagen.security.controller;

import com.koboolean.metagen.utils.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SecurityRestController {

    @GetMapping(value = "/isApprovalAvailable")
    public ResponseEntity<Map<String, Boolean>> isApprovalAvailable() {
        boolean isApprovalAvailable = AuthUtil.isIsApprovalAvailable();

        return ResponseEntity.ok(Map.of("result", isApprovalAvailable));
    }


}
