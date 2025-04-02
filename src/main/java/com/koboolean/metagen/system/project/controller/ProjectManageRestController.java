package com.koboolean.metagen.system.project.controller;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
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
@Tag(name = "Project Manage API", description = "프로젝트 관리 및 프로젝트 멤버 관리를 위한 API")
public class ProjectManageRestController {

    private final ProjectManageService projectManageService;

    @Operation(summary = "프로젝트 Member 컬럼 조회", description = "프로젝트 Member 테이블의 컬럼 목록을 조회합니다.")
    @GetMapping("/getProject/{projectId}/column")
    public ResponseEntity<List<ColumnDto>> getProjectColumn(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectManageService.getProjectColumn(projectId));
    }

    @Operation(summary = "프로젝트 Member 데이터 조회", description = "프로젝트 Member 데이터를 조회합니다.")
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

    @Operation(summary = "프로젝트 데이터 조회", description = "프로젝트 데이터를 조회합니다.")
    @GetMapping("/getProject/{projectId}/getEditProjectData")
    public ResponseEntity<Map<String,Object>> getEditProjectData(
            @Parameter(description = "프로젝트 ID", example = "0")
            @PathVariable Long projectId,
            @Parameter(description = "현재 로그인된 사용자 정보", hidden = true)
            @AuthenticationPrincipal AccountDto accountDto
    ) {
        ProjectDto projectDto = projectManageService.getEditProjectData(projectId, accountDto);

        return ResponseEntity.ok(Map.of("project", projectDto, "result", true));
    }

    @Operation(summary = "프로젝트 등록", description = "프로젝트를 등록합니다.")
    @PostMapping("/saveProject/project")
    public ResponseEntity<Map<String,Boolean>> createProject(@RequestBody ProjectDto projectDto, @AuthenticationPrincipal AccountDto accountDto) {
        projectManageService.createProject(projectDto, accountDto);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "프로젝트 수정", description = "프로젝트를 수정합니다.")
    @PutMapping("/saveProject/project")
    public ResponseEntity<Map<String,Boolean>> updateProject(@RequestBody ProjectDto projectDto) {
        projectManageService.updateProject(projectDto);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제합니다.")
    @DeleteMapping("/deleteProject/project/{selectedId}")
    public ResponseEntity<Map<String,Boolean>> deleteProject(@PathVariable Long selectedId, @AuthenticationPrincipal AccountDto accountDto) {
        projectManageService.deleteProject(selectedId, accountDto);
        return ResponseEntity.ok(Map.of("result", true));
    }
}
