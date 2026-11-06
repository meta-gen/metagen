package com.koboolean.metagen.chat.controller;

import com.koboolean.metagen.chat.service.ChatUserService;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 메신저 사용자 목록 API (기존 UserRestController 의 /api/activeUsers 를 이관, URL 동일)
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatUserRestController {

    private final ChatUserService chatUserService;

    @PreAuthorize("hasRole('NOT_APPROVE')")
    @GetMapping("/activeUsers")
    public ResponseEntity<Map<String,Object>> getActiveUsers(@AuthenticationPrincipal AccountDto accountDto) {
        Map<String, Object> result = chatUserService.getActiveUser(accountDto);

        return ResponseEntity.ok(Map.of(
                "result", true,
                "activeUsers", result.get("loggedIn"),
                "inactiveUsers", result.get("loggedOut")
        ));
    }
}
