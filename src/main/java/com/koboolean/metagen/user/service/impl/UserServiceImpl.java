package com.koboolean.metagen.user.service.impl;


import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.logs.domain.dto.LogsDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
import com.koboolean.metagen.security.domain.entity.Account;
import com.koboolean.metagen.system.project.domain.entity.Project;
import com.koboolean.metagen.system.project.domain.entity.ProjectMember;
import com.koboolean.metagen.security.domain.entity.Role;
import com.koboolean.metagen.security.exception.CustomFormException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.security.repository.RoleRepository;
import com.koboolean.metagen.security.repository.UserRepository;
import com.koboolean.metagen.system.project.repository.ProjectMemberRepository;
import com.koboolean.metagen.system.project.repository.ProjectRepository;
import com.koboolean.metagen.user.service.UserService;
import com.koboolean.metagen.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public List<ProjectDto> selectAllProjectsIsActive() {
        return projectRepository.findByIsActive(true).stream().map(ProjectDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<ColumnDto> selectUserColumn() {

        ColumnDto columnDto = new ColumnDto("권한", "role", ColumnType.STRING, RowType.SELECT, false);

        columnDto.setOptions(roleRepository.findAll().stream().map(Role::getRoleName).collect(Collectors.toList()));

        return List.of(
                new ColumnDto("","id", ColumnType.NUMBER, RowType.CHECKBOX),
                new ColumnDto("사용자 ID","username", ColumnType.STRING, true),
                new ColumnDto("사용자명","name", ColumnType.STRING, true),
                columnDto,
                new ColumnDto("권한 명", "roleName", ColumnType.STRING, RowType.TEXT, false),
                new ColumnDto("비밀번호 초기화", "resetButton", ColumnType.STRING, RowType.BUTTON, false)
        );
    }

    @Override
    public Page<AccountDto> selectUserData(Pageable pageable, AccountDto accountDto, String searchQuery, String searchColumn) {
        return userRepository.findAllByIsActive(true, pageable).map(AccountDto::fromEntity);
    }

    @Override
    @Transactional
    public void saveUser(List<AccountDto> accountDtos) {
        if(!AuthUtil.isIsApprovalAvailable()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        accountDtos.forEach(accountDto -> {

            Account account = userRepository.findById(Long.parseLong(accountDto.getId())).orElse(null);

            if(account != null){
                Role role = account.getUserRoles().stream().findFirst().get();

                // 권한이 변경된것을 확인하였으므로, 해당 권한으로 UPDATE한다.
                if(!role.getRoleName().equals(accountDto.getRole())){

                    if(accountDto.getId().equals("0")){
                        // admin 계정은 권한 변경이 불가능하다.
                        throw new CustomException(ErrorCode.MANAGER_NON_DELETED);
                    }

                    Role byRoleName = roleRepository.findByRoleName(accountDto.getRole());
                    account.setUserRoles(Set.of(byRoleName));
                }
            }
        });

    }

    @Override
    @Transactional
    public void deleteUser(List<AccountDto> accountDtos) {
        if(!AuthUtil.isIsApprovalAvailable()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        accountDtos.forEach(accountDto -> {
           Account account = userRepository.findById(Long.parseLong(accountDto.getId())).orElse(null);
           if(account != null){
               if(account.getId().equals(0L)){
                   throw new CustomException(ErrorCode.MANAGER_NON_DELETED);
               }

               account.setIsActive(false);
           }
        });
    }

    @Override
    @Transactional
    public void saveUserPassword(AccountDto accountDto) {
        if(!AuthUtil.isIsApprovalAvailable()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        Account account = userRepository.findById(Long.parseLong(accountDto.getId())).orElse(null);

        if(account != null){
            account.setPassword(passwordEncoder.encode(accountDto.getUsername()));
            account.setPasswdCheck(false);
        }
    }
}
