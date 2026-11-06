package com.koboolean.metagen.core.user.service.impl;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.project.domain.dto.ProjectDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.core.project.domain.entity.ProjectMember;
import com.koboolean.metagen.core.security.domain.entity.Role;
import com.koboolean.metagen.common.exception.CustomFormException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.security.repository.RoleRepository;
import com.koboolean.metagen.core.security.repository.UserRepository;
import com.koboolean.metagen.core.project.repository.ProjectMemberRepository;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.user.service.UserService;
import com.koboolean.metagen.core.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
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

        if(!AuthUtil.isValidPassword(accountDto.getPassword())){
            throw new CustomFormException(ErrorCode.PASSWORD_IS_NON_VALIDATOR);
        }

        if (!Pattern.matches("^[a-z0-9](?!.*__)[a-z0-9_]{3,18}[a-z0-9]$", accountDto.getUsername())) {
            throw new CustomFormException(ErrorCode.USERNAME_IS_NON_VALIDATOR);
        }

        Role role = roleRepository.findByRoleName("ROLE_NOT_APPROVE");
        Set<Role> roles = Set.of(role);
        account.setUserRoles(roles);
        account.setIsActive(true);
        userRepository.save(account);

        Project project = projectRepository.findById(accountDto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        ProjectMember projectMember = ProjectMember.builder()
                .project(project)
                .account(account)
                .isActive(project.getIsAutoActive())
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

    @Override
    public ProjectDto selectProjects(AccountDto accountDto) {
        return ProjectDto.fromEntity(Objects.requireNonNull(projectRepository.findById(accountDto.getProjectId()).orElse(null)));
    }

    @Override
    public List<ProjectDto> selectAllProjectsIsActive() {
        return projectRepository.findByIsActive(true).stream().map(ProjectDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<AccountDto> getAccountList() {
        return userRepository.findAll().stream().map(AccountDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    public String getProjectName(Long projectId) {
        return projectRepository.findById(projectId).orElse(Project.builder().projectName("NAN").build()).getProjectName();
    }

    @Override
    public ProjectMemberDto getProjectRoleName(Long projectId, String username) {
        return ProjectMemberDto.fromEntity(projectMemberRepository.findAllByProject_IdAndAccount_Username(projectId, username));
    }

    @Override
    public List<ProjectDto> selectAllProjectsByUsername(AccountDto accountDto) {
        Long accountId = Long.parseLong(accountDto.getId());

        return projectRepository.findAllByProjectMember_Account_Id(accountId).stream().map(ProjectDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<ProjectDto> selectAllProjectsByUsernameProjectManager(AccountDto accountDto) {
        Long accountId = Long.parseLong(accountDto.getId());

        return projectRepository.findAllByAccount_IdAndProjectMember_Account_Id(accountId, accountId).stream().map(ProjectDto::fromEntity).toList();
    }

}
