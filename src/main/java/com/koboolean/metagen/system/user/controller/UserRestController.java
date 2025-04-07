package com.koboolean.metagen.system.user.controller;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.logs.domain.dto.LogsDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.user.service.UserService;
import com.koboolean.metagen.utils.PageableUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "User Manage API", description = "사용자 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    @Operation(summary = "사용자 관리 컬럼 조회", description = "사용자 관리 컬럼 목록을 조회합니다.")
    @GetMapping("/selectUser/column")
    public ResponseEntity<List<ColumnDto>> selectUserColumn() {
        return ResponseEntity.ok(userService.selectUserColumn());
    }

    @Operation(summary = "사용자 관리 데이터 조회", description = "사용자 관리 그리드 데이터를 조회합니다.")
    @GetMapping("/selectUser/data")
    public ResponseEntity<Map<String,Object>> selectUserData(
            @Parameter(description = "사용자 인증 정보", hidden = true)
            @AuthenticationPrincipal AccountDto accountDto,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(value = "page") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(value = "size") int size,
            @Parameter(description = "정렬 조건 (예: timestamp,desc;id,asc)", example = "timestamp,desc")
            @RequestParam(required = false, value = "sort") String sort,
            @Parameter(description = "조회 도메인명", example = "도메인명1")
            @RequestParam(required = false, value = "searchQuery") String searchQuery,
            @Parameter(description = "조회컬럼 명", example = "id")
            @RequestParam(required = false, value = "searchColumn") String searchColumn) {

        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);

        Page<AccountDto> accountDtos = userService.selectUserData(pageable, accountDto, searchQuery, searchColumn);

        return PageableUtil.getGridPageableMap(accountDtos);
    }

    @Operation(summary = "사용자관리 권한 변경", description = "사용자 관리 그리드를 통해 사용자의 권한을 변경한다.")
    @PutMapping("/saveUser/update")
    public ResponseEntity<Map<String,Boolean>> saveUser(@RequestBody List<AccountDto> accountDtos) {
        userService.saveUser(accountDtos);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "사용자 삭제", description = "사용자 관리 그리드를 통해 사용자를 삭제한다.")
    @DeleteMapping("/deleteUser/user")
    public ResponseEntity<Map<String,Boolean>> deleteUser(@RequestBody List<AccountDto> accountDtos) {
        userService.deleteUser(accountDtos);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @PutMapping("/saveUser/password")
    public ResponseEntity<Map<String,Boolean>> saveUserPassword(@RequestBody AccountDto accountDto) {
        userService.saveUserPassword(accountDto);

        return ResponseEntity.ok(Map.of("result", true));
    }
}
