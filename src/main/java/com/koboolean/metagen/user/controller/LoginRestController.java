package com.koboolean.metagen.user.controller;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.user.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class LoginRestController {

    private final LoginService loginService;

    @Operation(summary = "비밀번호 변경", description = "현재 로그인된 사용자의 비밀번호를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping(value="/updatePwd")
    public ResponseEntity<Map<String, String>> updatePwd(
            @Parameter(description = "현재 로그인된 사용자 정보", hidden = true)
            @AuthenticationPrincipal AccountDto accountDto,

            @Parameter(description = "새로운 비밀번호 데이터", required = true)
            @RequestBody AccountDto accountData) {

        loginService.updatePwd(accountDto, accountData);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    @Operation(summary = "사용자 이름 변경", description = "현재 로그인된 사용자의 이름을 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이름 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping(value="/updateName")
    public ResponseEntity<Map<String, String>> updateName(
            @Parameter(description = "현재 로그인된 사용자 정보", hidden = true)
            @AuthenticationPrincipal AccountDto accountDto,

            @Parameter(description = "새로운 사용자 이름", required = true, example = "{\"name\": \"새로운 이름\"}")
            @RequestBody Map<String, String> nameMap) {

        loginService.updateName(accountDto, nameMap.get("name"));
        return ResponseEntity.ok(Map.of("result", "success"));
    }
}
