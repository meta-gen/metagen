package com.koboolean.metagen.system.project.controller;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.system.project.repository.ProjectMemberRepository;
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
    private final ProjectMemberRepository projectMemberRepository;

    @Operation(summary = "프로젝트 Member 컬럼 조회", description = "프로젝트 Member 테이블의 컬럼 목록을 조회합니다.")
    @GetMapping("/getProject/{projectId}/column")
    public ResponseEntity<List<ColumnDto>> getProjectColumn(@PathVariable(value = "projectId") Long projectId) {
        return ResponseEntity.ok(projectManageService.getProjectColumn(projectId));
    }

    @Operation(summary = "프로젝트 Member 데이터 조회", description = "프로젝트 Member 데이터를 조회합니다.")
    @GetMapping("/getProject/{projectId}/data")
    public ResponseEntity<Map<String,Object>> getProjectData(
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
            @PathVariable(value = "projectId") Long projectId
    ) {
        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<ProjectMemberDto> standardTermDataPage = projectManageService.getProjectData(pageable, searchColumn, searchQuery, projectId);
        return PageableUtil.getGridPageableMap(standardTermDataPage);
    }

    @Operation(summary = "프로젝트 데이터 조회", description = "프로젝트 데이터를 조회합니다.")
    @GetMapping("/getProject/{projectId}/getEditProjectData")
    public ResponseEntity<Map<String,Object>> getEditProjectData(
            @Parameter(description = "프로젝트 ID", example = "0")
            @PathVariable(value = "projectId") Long projectId,
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
    public ResponseEntity<Map<String,Boolean>> deleteProject(@PathVariable(value = "selectedId") Long selectedId, @AuthenticationPrincipal AccountDto accountDto) {
        projectManageService.deleteProject(selectedId, accountDto);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "프로젝트 멤버 승인/승인취소", description = "프로젝트 멤버의 승인여부(승인/승인취소)로 변경합니다.")
    @PostMapping("/saveProject/active/{isActive}")
    public ResponseEntity<Map<String, Boolean>> saveActiveProject(@PathVariable(value = "isActive") Boolean isActive, @RequestBody List<ProjectMemberDto> projectMemberDtos) {
        projectManageService.saveActiveProject(projectMemberDtos, isActive);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "프로젝트 멤버 삭제", description = "프로젝트 멤버를 삭제처리합니다.")
    @DeleteMapping("/deleteProject/projectMember")
    public ResponseEntity<Map<String, Boolean>> deleteProjectMember(@RequestBody List<ProjectMemberDto> projectMemberDtos, @AuthenticationPrincipal AccountDto accountDto) {
        projectManageService.deleteProjectMember(projectMemberDtos, accountDto);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "프로젝트 멤버 조회", description = "추가를위한 프로젝트 멤버를 조회합니다.")
    @GetMapping("/getProject/projectMember/{selectedId}")
    public ResponseEntity<Map<String, Object>> selectProjectMember(@PathVariable(value = "selectedId") Long selectedId, @AuthenticationPrincipal AccountDto accountDto) {
        List<AccountDto> addProjectMember = projectManageService.selectProjectMember(selectedId, accountDto);
        return ResponseEntity.ok(Map.of("result", true,"projectMember", addProjectMember));
    }

    @PostMapping("/saveProject/projectMember/{projectId}")

    public ResponseEntity<Map<String,Boolean>> saveProjectMember(@RequestBody Map<String, String> accountIds, @PathVariable(value = "projectId") Long projectId) {
        projectManageService.saveProjectMember(accountIds, projectId);
        return ResponseEntity.ok(Map.of("result", true));
    }
}
