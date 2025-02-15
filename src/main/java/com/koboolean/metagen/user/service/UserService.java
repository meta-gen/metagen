package com.koboolean.metagen.user.service;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.domain.entity.Account;
import org.springframework.security.core.Authentication;

public interface UserService {
    AccountDto updateAccount(AccountDto accountDto);

    String getRoleName(Authentication authentication);

    void createUser(Account account);
}
