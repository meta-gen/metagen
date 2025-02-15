package com.koboolean.metagen.user.service;


import com.koboolean.metagen.security.domain.dto.AccountDto;

public interface LoginService {

    void updatePwd(AccountDto accountDto, AccountDto accountData);

    void updateName(AccountDto accountDto, String name);
}
