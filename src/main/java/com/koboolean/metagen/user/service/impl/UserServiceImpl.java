package com.koboolean.metagen.user.service.impl;


import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.domain.dto.ProjectDto;
import com.koboolean.metagen.security.domain.entity.Account;
import com.koboolean.metagen.security.domain.entity.Project;
import com.koboolean.metagen.security.domain.entity.ProjectMember;
import com.koboolean.metagen.security.domain.entity.Role;
import com.koboolean.metagen.security.exception.CustomFormException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.security.repository.RoleRepository;
import com.koboolean.metagen.security.repository.UserRepository;
import com.koboolean.metagen.user.repository.ProjectMemberRepository;
import com.koboolean.metagen.user.repository.ProjectRepository;
import com.koboolean.metagen.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final RoleRepository roleRepository;
    private final ProjectRepository projectRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createUser(AccountDto accountDto) {
        ModelMapper mapper = new ModelMapper();
        Account account = mapper.map(accountDto, Account.class);
        account.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        account.setPasswdCheck(true);

        Account user = userRepository.findByUsername(account.getUsername());

        if(user != null){
            throw new CustomFormException(ErrorCode.USERNAME_IS_DUPLICATION);
        }

        Role role = roleRepository.findByRoleName("ROLE_NOT_APPROVE");
        Set<Role> roles = Set.of(role);
        account.setUserRoles(roles);
        userRepository.save(account);

        ProjectMember projectMember = ProjectMember.builder()
                .projectId(accountDto.getProjectId())
                .account(account)
                .isActive(false)
                .build();

        projectMemberRepository.save(projectMember);
    }

    @Override
    public AccountDto setName(AccountDto accountDto) {
        Account byUsername = userRepository.findByUsername(accountDto.getUsername());
        accountDto.setName(byUsername.getName());

        updateNewAccount(accountDto);

        return accountDto;
    }

    /**
     * AccountDTO 변경사항 적용하기
     * */
    private void updateNewAccount(AccountDto accountDto) {
        // 현재 SecurityContext에서 Authentication 가져오기
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        // 새로운 AccountDto로 Principal 교체
        UsernamePasswordAuthenticationToken newAuthentication =
                new UsernamePasswordAuthenticationToken(
                        accountDto, // 변경된 계정 정보
                        authentication.getCredentials(), // 기존 인증 정보
                        authentication.getAuthorities() // 기존 권한 정보
                );

        // SecurityContext에 새 Authentication 설정
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }

    public String getRoleName(String authentication) {
        return roleRepository.findByRoleName(authentication).getRoleDesc();
    }

    public List<ProjectDto> selectProject(String username) {

        List<Project> projectsByUsername = projectRepository.findProjectsByUsername(username);

        List<ProjectDto> list = new ArrayList<>();
        projectsByUsername.forEach(project -> {
            list.add(ProjectDto.fromEntity(project));
        });

        return list;
    }

    @Override
    public List<ProjectDto> selectAllProjects() {
        return projectRepository.findAll().stream().map(ProjectDto::fromEntity).collect(Collectors.toList());
    }
}
