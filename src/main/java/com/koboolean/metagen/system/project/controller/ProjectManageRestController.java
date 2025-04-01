package com.koboolean.metagen.system.project.controller;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.system.project.service.ProjectManageService;
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
@Tag(name = "Data Dictionary API", description = "데이터 사전 관리 API")
public class ProjectManageRestController {

    private final ProjectManageService projectManageService;

    @Operation(summary = "표준 용어 컬럼 조회", description = "표준 용어 테이블의 컬럼 목록을 조회합니다.")
    @GetMapping("/getProject/{projectId}/column")
    public ResponseEntity<List<ColumnDto>> getProjectColumn(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectManageService.getProjectColumn());
    }

    @Operation(summary = "표준 용어 데이터 조회", description = "표준 용어 데이터를 조회합니다.")
    @GetMapping("/getProject/{projectId}/data")
    public ResponseEntity<Map<String,Object>> getProjectData(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam int size,
            @Parameter(description = "정렬 조건 (예: timestamp,desc;id,asc)", example = "id,desc")
            @RequestParam(required = false) String sort,
            @Parameter(description = "조회 용어명", example = "용어명1")
            @RequestParam(required = false) String searchQuery,
            @Parameter(description = "조회컬럼 명", example = "id")
            @RequestParam(required = false) String searchColumn,
            @Parameter(description = "프로젝트 ID", example = "0")
            @PathVariable Long projectId
    ) {
        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<ProjectMemberDto> standardTermDataPage = projectManageService.getProjectData(pageable, searchColumn, searchQuery, projectId);
        return PageableUtil.getGridPageableMap(standardTermDataPage);
    }
}
