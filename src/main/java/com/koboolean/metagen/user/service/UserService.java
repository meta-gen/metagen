package com.koboolean.metagen.user.service;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.domain.dto.ProjectDto;
import com.koboolean.metagen.security.domain.entity.Account;

import java.util.List;

public interface UserService {
    AccountDto setName(AccountDto accountDto);

    String getRoleName(String authentication);

    void createUser(AccountDto accountDto);

    List<ProjectDto> selectProject(String username);

    List<ProjectDto> selectAllProjects();
}
