package com.koboolean.metagen.core.login.service.impl;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.CustomFormException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.security.repository.UserRepository;
import com.koboolean.metagen.core.project.domain.dto.ProjectDto;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.login.service.LoginService;
import com.koboolean.metagen.core.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginServiceImpl implements LoginService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    /**
     * 로그인 전 단계 : 1. 로그인 정보(아이디/비밀번호)를 확인한 뒤 2. 접근 가능한 프로젝트를 조회한다.
     *
     * @param username
     * @param password
     * @return 접근 가능한 프로젝트 목록 (1건 이상)
     */
    @Override
    public List<ProjectDto> selectLoginProjects(String username, String password) {

        Account account = userRepository.findByUsernameAndIsActive(username, true);

        if (account == null || password == null || !passwordEncoder.matches(password, account.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        List<ProjectDto> projects = projectRepository.findProjectsByUsername(username).stream()
                .map(ProjectDto::fromEntity)
                .toList();

        if (projects.isEmpty()) {
            throw new CustomException(ErrorCode.NO_ACCESSIBLE_PROJECT);
        }

        return projects;
    }

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

            if(!AuthUtil.isValidPassword(accountData.getCurrentPassword())){
                throw new CustomException(ErrorCode.PASSWORD_IS_NON_VALIDATOR);
            }

            byUsername.setPassword(passwordEncoder.encode(accountData.getCurrentPassword()));
            if(!byUsername.isPasswdCheck()){
                byUsername.setPasswdCheck(true);
            }
        }else{
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHED);
        }
    }

    @Transactional
    public void updateName(AccountDto accountDto, String name) {
        userRepository.findByUsername(accountDto.getUsername()).setName(name);
    }
}
