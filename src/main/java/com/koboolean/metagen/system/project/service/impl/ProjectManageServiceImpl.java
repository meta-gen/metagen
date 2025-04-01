package com.koboolean.metagen.system.project.service.impl;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.system.project.domain.entity.ProjectMember;
import com.koboolean.metagen.system.project.service.ProjectManageService;
import com.koboolean.metagen.system.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectManageServiceImpl implements ProjectManageService {

    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public List<ColumnDto> getProjectColumn() {

        /*private Long id;
        private Long projectId;
        private String username;
        private String name;
        private Boolean isActive;*/

        return List.of(new ColumnDto("id", "id", ColumnType.NUMBER, RowType.CHECKBOX),
                new ColumnDto("프로젝트명", "projectName", ColumnType.STRING),
                new ColumnDto("아이디", "username", ColumnType.STRING),
                new ColumnDto("사용자명", "name", ColumnType.STRING),
                new ColumnDto("활성화여부", "isActive", ColumnType.STRING, true));
    }

    @Override
    public Page<ProjectMemberDto> getProjectData(Pageable pageable, String searchColumn, String searchQuery, Long projectId) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // 검색어가 없을 경우 전체 조회
            Page<ProjectMember> allByProjectId = projectMemberRepository.findAllByProject_Id(pageable, projectId);
            return allByProjectId.map(ProjectMemberDto::fromEntity);
        }

        return null;
    }
}
