package com.koboolean.metagen.system.code.controller;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.code.service.CodeRuleManageService;
import com.koboolean.metagen.system.project.domain.dto.TemplateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
}
