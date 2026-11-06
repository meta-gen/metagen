package com.koboolean.metagen.core.login.service;


import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.project.domain.dto.ProjectDto;

import java.util.List;

public interface LoginService {

    List<ProjectDto> selectLoginProjects(String username, String password);

    void changeInitialPassword(String username, String password, String newPassword);

    void updatePwd(AccountDto accountDto, AccountDto accountData);

    void updateName(AccountDto accountDto, String name);
}
