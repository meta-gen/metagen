package com.koboolean.metagen.user.controller;

import com.koboolean.metagen.domain.dto.AccountDto;
import com.koboolean.metagen.user.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginRestController {

    private final LoginService loginService;

    @PostMapping(value="/updatePasswd")
    public String updatePasswd(@AuthenticationPrincipal AccountDto accountDto, HttpServletRequest request) {

        loginService.updatePasswd(accountDto, request);

        return "success";
    }
}
