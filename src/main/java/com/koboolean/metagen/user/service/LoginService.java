package com.koboolean.metagen.user.service;


import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectDto;

import java.util.List;

public interface LoginService {

    List<ProjectDto> selectLoginProjects(String username, String password);

    void updatePwd(AccountDto accountDto, AccountDto accountData);

    void updateName(AccountDto accountDto, String name);
}
