package com.koboolean.metagen.user.service.impl;


import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.board.domain.dto.BoardViewDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.home.domain.dto.DashboardDto;
import com.koboolean.metagen.home.domain.dto.RecentChangeDto;
import com.koboolean.metagen.redis.service.MessageSaveService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
import com.koboolean.metagen.security.domain.entity.Account;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.system.project.domain.entity.Project;
import com.koboolean.metagen.system.project.domain.entity.ProjectMember;
import com.koboolean.metagen.security.domain.entity.Role;
import com.koboolean.metagen.security.exception.CustomFormException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.security.repository.RoleRepository;
import com.koboolean.metagen.security.repository.UserRepository;
import com.koboolean.metagen.system.project.repository.ProjectMemberRepository;
import com.koboolean.metagen.system.project.repository.ProjectRepository;
import com.koboolean.metagen.user.repository.DashboardRepository;
import com.koboolean.metagen.user.service.UserService;
import com.koboolean.metagen.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final MessageSaveService messageSaveService;

    private final PasswordEncoder passwordEncoder;

    private final RedisTemplate<String, Object> redisTemplate;

    private final DashboardRepository dashboardRepository;

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
        if(!AuthUtil.isApprovalAvailable()){
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
        if(!AuthUtil.isApprovalAvailable()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN");

        accountDtos.forEach(accountDto -> {
            Account account = userRepository.findById(Long.parseLong(accountDto.getId()))
                    .orElse(null);

            if(account != null){
                boolean isAdmin = account.getUserRoles().contains(adminRole);

                if (isAdmin) {
                    long activeAdminCount = adminRole.getAccounts().stream()
                            .filter(Account::getIsActive)
                            .distinct()
                            .count();

                    if (activeAdminCount <= 1) {
                        throw new CustomException(ErrorCode.MANAGER_NON_DELETED);
                    }
                }

                account.setIsActive(false); // soft delete
            }
        });
    }

    @Override
    @Transactional
    public void saveUserPassword(AccountDto accountDto) {
        if(!AuthUtil.isApprovalAvailable()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        Account account = userRepository.findById(Long.parseLong(accountDto.getId())).orElse(null);

        if(account != null){
            account.setPassword(passwordEncoder.encode(accountDto.getUsername()));
            account.setPasswdCheck(false);
        }
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
    public Map<String, Object> getActiveUser(AccountDto accountDto) {

        Map<String, Object> result = new HashMap<>();

        List<AccountDto> accountDtos = getAccountList(); // 전체 사용자
        Set<String> keys = redisTemplate.keys("login:user:*");

        Map<String, Map<Object, Object>> redisDataMap = new HashMap<>();

        if (keys != null) {
            for (String key : keys) {
                Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
                if (!data.isEmpty()) {
                    redisDataMap.put(key, data);
                }
            }
        }

        List<Map<String, String>> loggedIn = new ArrayList<>();
        List<Map<String, String>> loggedOut = new ArrayList<>();

        for (AccountDto dto : accountDtos) {
            String redisKey = "login:user:" + dto.getId();

            if (redisDataMap.containsKey(redisKey)) {
                Map<Object, Object> redisData = redisDataMap.get(redisKey);

                Long projectId = Long.parseLong(String.valueOf(redisData.get("projectId")));

                String username = dto.getUsername();
                String projectName = getProjectName(projectId);
                ProjectMemberDto projectRole = getProjectRoleName(projectId, username);

                Map<String, String> user = new HashMap<>();

                if ("O".equals(projectRole.getProjectManagerYn())) {
                    user.put("role", "매니저");
                } else {
                    user.put("role", "사용자");
                }

                user.put("isMyData", dto.getId().equals(accountDto.getId()) ? "true" : "false");
                user.put("id", dto.getId());
                user.put("name", dto.getName());
                user.put("project", projectName);
                user.put("status", "active");
                user.put("badge", messageSaveService.findByBadge(dto.getId(), accountDto.getId()));

                loggedIn.add(user);
            } else {
                Map<String, String> user = new HashMap<>();
                user.put("id", dto.getId());
                user.put("username", dto.getUsername());
                user.put("name", dto.getName());
                user.put("status", "inactive");
                user.put("isMyData", "false");
                user.put("badge", messageSaveService.findByBadge(dto.getId(), accountDto.getId()));

                loggedOut.add(user);
            }
        }
        result.put("loggedIn", loggedIn);
        result.put("loggedOut", loggedOut);

        return result;
    }

    @Override
    public DashboardDto selectDashboardData(AccountDto accountDto) {

        Long projectId = accountDto.getProjectId();

        List<RecentChangeDto> recentChanges = dashboardRepository.findRecentChanges(projectId, 10);
        List<BoardViewDto> notice = dashboardRepository.findNotice(projectId, 3);

        DashboardDto dashboardDto = dashboardRepository.findDataCount(projectId);

        dashboardDto.setRecentChanges(recentChanges);
        dashboardDto.setNotices(notice);

        return dashboardDto;
    }


}
