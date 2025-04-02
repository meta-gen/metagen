package com.koboolean.metagen.system.project.service;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectManageService {
    List<ColumnDto> getProjectColumn(Long projectId);
    Page<ProjectMemberDto> getProjectData(Pageable pageable, String searchColumn, String searchQuery, Long projectId);

    ProjectDto getEditProjectData(Long projectId, AccountDto accountDto);

    void createProject(ProjectDto projectDto, AccountDto accountDto);
    void updateProject(ProjectDto projectDto);
    void deleteProject(Long selectedId, AccountDto accountDto);

    boolean selectLoginProject(Long projectId, String id);
}
