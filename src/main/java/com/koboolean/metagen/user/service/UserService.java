package com.koboolean.metagen.user.service;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.logs.domain.dto.LogsDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
}
