package com.koboolean.metagen.user.controller;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.user.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class LoginRestController {

    private final LoginService loginService;

    @PostMapping(value="/updatePwd")
    public String updatePwd(@AuthenticationPrincipal AccountDto accountDto, @RequestBody AccountDto accountData) {

        loginService.updatePwd(accountDto, accountData);

        return "success";
    }

    @PostMapping(value="/updateName")
    public String updateName(@AuthenticationPrincipal AccountDto accountDto, @RequestBody Map<String,String> nameMap) {

        loginService.updateName(accountDto, nameMap.get("name"));

        return "success";
    }
}
