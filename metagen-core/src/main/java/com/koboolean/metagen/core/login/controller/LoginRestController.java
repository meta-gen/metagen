package com.koboolean.metagen.core.login.controller;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.project.domain.dto.ProjectDto;
import com.koboolean.metagen.core.login.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
@Tag(name = "Login Rest API", description = "사용자 로그인 관련 API")
public class LoginRestController {

    private final LoginService loginService;

    @Operation(summary = "비밀번호 변경", description = "현재 로그인된 사용자의 비밀번호를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PreAuthorize("hasRole('NOT_APPROVE')")
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
    @PreAuthorize("hasRole('NOT_APPROVE')")
    @PostMapping(value="/updateName")
    public ResponseEntity<Map<String, String>> updateName(
            @Parameter(description = "현재 로그인된 사용자 정보", hidden = true)
            @AuthenticationPrincipal AccountDto accountDto,

            @Parameter(description = "새로운 사용자 이름", required = true, example = "{\"name\": \"새로운 이름\"}")
            @RequestBody Map<String, String> nameMap) {

        loginService.updateName(accountDto, nameMap.get("name"));
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    @Operation(summary = "로그인 가능 프로젝트 조회", description = "아이디/비밀번호를 먼저 확인한 뒤, 접근 가능한 프로젝트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치"),
            @ApiResponse(responseCode = "403", description = "접근 가능한 프로젝트 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PreAuthorize("hasRole('ANONYMOUS')")
    @PostMapping(value="/login/projects")
    public ResponseEntity<List<Map<String, Object>>> selectLoginProjects(
            @Parameter(description = "로그인 정보", required = true, example = "{\"username\": \"admin\", \"password\": \"admin\"}")
            @RequestBody Map<String, String> loginData) {

        List<ProjectDto> projects = loginService.selectLoginProjects(loginData.get("username"), loginData.get("password"));

        // 로그인 완료 전이므로 프로젝트 선택에 필요한 정보(id, 프로젝트명)만 반환
        return ResponseEntity.ok(projects.stream()
                .map(project -> Map.<String, Object>of("id", project.getId(), "projectName", project.getProjectName()))
                .toList());
    }
}
