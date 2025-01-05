package com.koboolean.metagen.user.service;

import com.koboolean.metagen.domain.dto.AccountDto;
import com.koboolean.metagen.domain.entity.Account;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /**
     * 패스워드 변경
     *
     * @param accountDto
     * @param accountData
     */
    @Transactional
    public void updatePwd(AccountDto accountDto, AccountDto accountData) {

        Account byUsername = userRepository.findByUsername(accountDto.getUsername());

        if(passwordEncoder.matches(accountData.getPassword(), byUsername.getPassword())) {
            byUsername.setPassword(passwordEncoder.encode(accountData.getCurrentPassword()));
        }else{
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHED);
        }
    }

    @Transactional
    public void updateName(AccountDto accountDto, String name) {
        userRepository.findByUsername(accountDto.getUsername()).setName(name);
    }
}
