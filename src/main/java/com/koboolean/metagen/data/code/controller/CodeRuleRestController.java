package com.koboolean.metagen.data.code.controller;

import com.koboolean.metagen.data.code.domain.dto.CodeRuleDetailDto;
import com.koboolean.metagen.data.code.service.CodeRuleService;
import com.koboolean.metagen.data.column.domain.dto.ColumnInfoDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.code.domain.dto.CodeRuleDto;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Code Rule API", description = "코드규칙 API")
public class CodeRuleRestController {

    private final CodeRuleService codeRuleService;

    @Operation(summary = "코드규칙 컬럼 조회", description = "코드규칙 내 컬럼 목록을 조회합니다.")
    @GetMapping("/selectCodeRule/column")
    public ResponseEntity<List<ColumnDto>> selectCodeRuleColumn() {
        return ResponseEntity.ok(codeRuleService.selectCodeRuleColumn());
    }

    @Operation(summary = "코드규칙 데이터 조회", description = "코드규칙 내 데이터 목록을 조회합니다.")
    @GetMapping("/selectCodeRule/data")
    public ResponseEntity<Map<String,Object>> selectCodeRuleData(
            @Parameter(description = "사용자 인증 정보", hidden = true)
            @AuthenticationPrincipal AccountDto accountDto,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(value = "page") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(value = "size") int size,
            @Parameter(description = "정렬 조건 (예: timestamp,desc;id,asc)", example = "id,desc")
            @RequestParam(required = false, value = "sort") String sort,
            @Parameter(description = "조회 용어명", example = "용어명1")
            @RequestParam(required = false, value = "searchQuery") String searchQuery,
            @Parameter(description = "조회컬럼 명", example = "id")
            @RequestParam(required = false, value = "searchColumn") String searchColumn,
            @Parameter(description = "테이블 ID", example = "0")
            @RequestParam(required = false, value = "tableId") Long tableId
    ) {
        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<CodeRuleDetailDto> codeRuleDetailDtos = codeRuleService.selectCodeRuleData(pageable, accountDto, searchColumn, searchQuery, tableId);
        return PageableUtil.getGridPageableMap(codeRuleDetailDtos);
    }

    @Operation(summary = "코드규칙 조회", description = "템플릿에 등록된 코드규칙을 조회한다.")
    @GetMapping("/selectCodeRule/codeRule/{codeRuleId}")
    public ResponseEntity<Map<String, Object>> selectCodeRuleDetailById(@AuthenticationPrincipal AccountDto accountDto, @PathVariable(value = "codeRuleId") Long codeRuleId) {
        List<CodeRuleDto> codeRuleDtos = codeRuleService.selectCodeRuleDetailById(accountDto, codeRuleId);

        return ResponseEntity.ok(Map.of("result", true, "codeRules", codeRuleDtos));
    }

    @Operation(summary = "코드규칙 변환", description = "입력한 코드규칙으로 변환 수행한다.")
    @PostMapping("/selectCodeRule/detail")
    public ResponseEntity<Map<String,Object>> selectCodeRuleDetail(@AuthenticationPrincipal AccountDto accountDto, @RequestBody CodeRuleDetailDto codeRuleDetailDto){
        CodeRuleDetailDto codeRuleDto = codeRuleService.selectCodeRuleDetail(accountDto, codeRuleDetailDto);

        return ResponseEntity.ok(Map.of("result", true, "codeRule", codeRuleDto));
    }

    @Operation(summary = "코드규칙 등록", description = "입력한 코드규칙을 등록한다.")
    @PostMapping("/saveCodeRule")
    public ResponseEntity<Map<String, Object>> insertCodeRule(@AuthenticationPrincipal AccountDto accountDto, @RequestBody CodeRuleDetailDto codeRuleDetailDto) {
        codeRuleService.insertCodeRule(accountDto, codeRuleDetailDto);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "코드규칙 수정", description = "입력한 코드규칙을 수정한다.")
    @PutMapping("/saveCodeRule")
    public ResponseEntity<Map<String, Object>> updateCodeRule(@AuthenticationPrincipal AccountDto accountDto, @RequestBody CodeRuleDetailDto codeRuleDetailDto) {
        codeRuleService.updateCodeRule(accountDto, codeRuleDetailDto);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "코드규칙 상세조회", description = "입력한 코드규칙을 상세조회한다.")
    @GetMapping("/selectCodeRule/detail/{id}")
    public ResponseEntity<Map<String, Object>> selectCodeRuleDetail(@AuthenticationPrincipal AccountDto accountDto, @PathVariable(value = "id") Long id) {
        CodeRuleDetailDto dto = codeRuleService.selectCodeRuleDetailDataById(accountDto, id);
        return ResponseEntity.ok(Map.of("result", true, "codeRule", dto));
    }
}
