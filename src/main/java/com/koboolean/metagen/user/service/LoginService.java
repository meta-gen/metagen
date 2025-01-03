package com.koboolean.metagen.user.service;

import com.koboolean.metagen.domain.dto.AccountDto;
import com.koboolean.metagen.domain.entity.Account;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.SecretException;
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
     * @param request
     */
    @Transactional
    public void updatePasswd(AccountDto accountDto, HttpServletRequest request) {

        Account byUsername = userRepository.findByUsername(accountDto.getUsername());

        if(passwordEncoder.matches(request.getParameter("password"), byUsername.getPassword())) {
            byUsername.setPassword(passwordEncoder.encode(request.getParameter("changedPasswd")));
        }else{
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHED);
        }
    }
}
