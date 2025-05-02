package com.koboolean.metagen.user.service;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.home.domain.dto.DashboardDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface UserService {
    AccountDto setName(AccountDto accountDto);

    String getRoleName(String authentication);

    void createUser(AccountDto accountDto);

    List<ProjectDto> selectProject(String username);

    List<ProjectDto> selectAllProjects();

    List<ProjectDto> selectAllProjectsIsActive();

    List<ColumnDto> selectUserColumn();

    Page<AccountDto> selectUserData(Pageable pageable, AccountDto accountDto, String searchQuery, String searchColumn);

    void saveUser(List<AccountDto> accountDtos);

    void deleteUser(List<AccountDto> accountDtos);

    void saveUserPassword(AccountDto accountDto);

    List<AccountDto> getAccountList();

    String getProjectName(Long projectId);

    ProjectMemberDto getProjectRoleName(Long projectId, String username);

    Map<String, Object> getActiveUser(AccountDto accountDto);

    DashboardDto selectDashboardData(AccountDto accountDto);
}
