package com.koboolean.metagen.system.code.controller;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.code.service.CodeRuleManageService;
import com.koboolean.metagen.system.project.domain.dto.CodeRuleDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.system.project.domain.dto.TemplateDto;
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
@Tag(name = "Code Rule Manage Rest API", description = "코드규칙 관리 API")
public class CodeRuleManageRestController {

    private final CodeRuleManageService codeRuleManageService;

    @Operation(summary = "코드규칙 템플릿 등록", description = "프로젝트 내 코드규칙의 템플릿을 등록한다.")
    @PostMapping("/saveCodeRuleManage/template/{projectId}")
    public ResponseEntity<Map<String, Boolean>> saveCodeRuleManageTemplate(@RequestBody TemplateDto template, @PathVariable(value = "projectId") Long projectId) {
        codeRuleManageService.saveCodeRuleManageTemplate(projectId, template);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "템플릿 목록 조회", description = "프로젝트에 해당하는 템플릿 목록을 조회한다.")
    @GetMapping("/selectCodeRuleManage/template/{projectId}")
    public ResponseEntity<Map<String, Object>> selectCodeRuleManageTemplate(@PathVariable(value = "projectId") Long projectId) {
        List<TemplateDto> templateDtos = codeRuleManageService.selectCodeRuleManageTemplate(projectId);

        return ResponseEntity.ok(Map.of("result", true, "templates", templateDtos));
    }

    @Operation(summary = "템플릿 삭제", description = "프로젝트에 해당하는 템플릿을 삭제한다.")
    @DeleteMapping("/deleteCodeRuleManage/template/{selectedId}")
    public ResponseEntity<Map<String, Boolean>> deleteCodeRuleManageTemplate(@PathVariable(value = "selectedId") Long selectedId) {
        codeRuleManageService.deleteCodeRuleManageTemplate(selectedId);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "코드규칙관리 컬럼조회", description = "코드규칙관리 컬럼을 조회한다.")
    @GetMapping("/selectCodeRuleManage/{projectId}/column")
    public ResponseEntity<List<ColumnDto>> getCodeRuleManageColumn(@PathVariable(value = "projectId") Long projectId) {
        return ResponseEntity.ok(codeRuleManageService.getCodeRuleManageColumn());
    }

    @Operation(summary = "코드규칙관리 데이터조회", description = "코드규칙관리 데이터를 조회한다.")
    @GetMapping("/selectCodeRuleManage/{projectId}/data")
    public ResponseEntity<Map<String,Object>> getCodeRuleManageData(
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
            @Parameter(description = "프로젝트 ID", example = "0")
            @PathVariable(value = "projectId") Long projectId){

        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<CodeRuleDto> standardTermDataPage = codeRuleManageService.getCodeRuleManageData(pageable, searchColumn, searchQuery, projectId);
        return PageableUtil.getGridPageableMap(standardTermDataPage);
    }

}
