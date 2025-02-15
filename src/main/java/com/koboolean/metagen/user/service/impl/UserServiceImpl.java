package com.koboolean.metagen.user.service.impl;


import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.domain.entity.Account;
import com.koboolean.metagen.security.domain.entity.Role;
import com.koboolean.metagen.security.exception.CustomFormException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.security.repository.RoleRepository;
import com.koboolean.metagen.security.repository.UserRepository;
import com.koboolean.metagen.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public void createUser(Account account) {

        Account user = userRepository.findByUsername(account.getUsername());

        if(user != null){
            throw new CustomFormException(ErrorCode.USERNAME_IS_DUPLICATION);
        }

        Role role = roleRepository.findByRoleName("ROLE_NOT_APPROVE");
        Set<Role> roles = Set.of(role);
        account.setUserRoles(roles);
        userRepository.save(account);
    }

    public String getRoleName(Authentication authentication) {

        String role = ((List) authentication.getAuthorities()).get(0).toString();

        return roleRepository.findByRoleName(role).getRoleDesc();
    }

    public AccountDto updateAccount(AccountDto accountDto) {
        ModelMapper mapper = new ModelMapper();
        Account account = userRepository.findByUsername(accountDto.getUsername());

        return mapper.map(account, AccountDto.class);
    }
}
