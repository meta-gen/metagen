package com.koboolean.metagen.data.commoncolumn.controller;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.util.AuthUtil;
import com.koboolean.metagen.data.commoncolumn.domain.dto.CommonColumnDto;
import com.koboolean.metagen.data.commoncolumn.service.CommonColumnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Common Column API", description = "프로젝트 공통 컬럼 세트 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommonColumnRestController {

    private final CommonColumnService commonColumnService;

    @Operation(summary = "공통 컬럼 세트 조회", description = "로그인 프로젝트의 공통 컬럼 세트를 순서대로 조회합니다. editable 은 세트를 바꿀 수 있는지(프로젝트 관리자) 여부입니다.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/commonColumn")
    public ResponseEntity<Map<String, Object>> selectCommonColumns(@AuthenticationPrincipal AccountDto accountDto) {
        return ResponseEntity.ok(Map.of(
                "result", true,
                "editable", AuthUtil.isApprovalAvailable(),
                "items", commonColumnService.selectCommonColumns(accountDto)));
    }

    @Operation(summary = "공통 컬럼 세트 저장", description = "공통 컬럼 세트를 목록 순서대로 통째로 바꿉니다. 프로젝트 관리자(또는 ADMIN/MANAGER)만 가능합니다.")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/commonColumn")
    public ResponseEntity<Map<String, Boolean>> replaceCommonColumns(@RequestBody List<CommonColumnDto> items, @AuthenticationPrincipal AccountDto accountDto) {
        commonColumnService.replaceCommonColumns(accountDto, items);
        return ResponseEntity.ok(Map.of("result", true));
    }
}
