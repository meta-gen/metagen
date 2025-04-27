package com.koboolean.metagen.system.project.service.impl;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.domain.entity.Account;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.security.repository.AccountRepository;
import com.koboolean.metagen.system.code.repository.TemplateRepository;
import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.system.project.domain.entity.Project;
import com.koboolean.metagen.system.project.domain.entity.ProjectMember;
import com.koboolean.metagen.system.project.repository.ProjectRepository;
import com.koboolean.metagen.system.project.service.ProjectManageService;
import com.koboolean.metagen.system.project.repository.ProjectMemberRepository;
import com.koboolean.metagen.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectManageServiceImpl implements ProjectManageService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;
    private final TemplateRepository templateRepository;

    @Override
    public List<ColumnDto> getProjectColumn(Long projectId) {
        ColumnDto columnDto = new ColumnDto("활성화여부", "isActive", ColumnType.STRING, RowType.TEXT, true,false);

        // List<String> options = List.of("Y", "N");
        // columnDto.setOptions(options);

        return List.of(new ColumnDto("id", "id", ColumnType.NUMBER, RowType.CHECKBOX, false),
                new ColumnDto("프로젝트명", "projectName", ColumnType.STRING, RowType.TEXT, false),
                new ColumnDto("아이디", "username", ColumnType.STRING, RowType.TEXT, false),
                new ColumnDto("사용자명", "name", ColumnType.STRING, RowType.TEXT, false),
                new ColumnDto("프로젝트 관리자", "projectManagerYn", ColumnType.STRING, RowType.TEXT, false),
                columnDto);
    }

    @Override
    public Page<ProjectMemberDto> getProjectData(Pageable pageable, String searchColumn, String searchQuery, Long projectId) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // 검색어가 없을 경우 전체 조회
            Page<ProjectMember> allByProjectId = projectMemberRepository.findAllByProject_Id(pageable, projectId);
            return allByProjectId.map(ProjectMemberDto::fromEntity);
        }

        return projectMemberRepository.findAllByIsActiveAndProject_id(pageable, searchQuery.equals("Y"),projectId).map(ProjectMemberDto::fromEntity);
    }

    @Override
    public ProjectDto getEditProjectData(Long projectId, AccountDto accountDto) {
        return projectRepository.findById(projectId).map(m -> ProjectDto.fromEntity(m, accountDto)).orElse(null);
    }

    @Override
    @Transactional
    public void createProject(ProjectDto projectDto, AccountDto accountDto) {

        Account account = accountRepository.findById(Long.parseLong(accountDto.getId())).orElse(null);

        Project project = Project.builder()
                .projectName(projectDto.getProjectName())
                .isActive(projectDto.getIsActive())
                .account(account)
                .isAutoActive(projectDto.getIsAutoActive())
                .isDicAbbrUsed(projectDto.getIsDicAbbrUsed())
                .build();

        ProjectMember projectMember = ProjectMember.builder()
                .project(project)
                .isActive(true)
                .account(account)
                .build();

        projectMemberRepository.save(projectMember);

        project.setProjectMembers(List.of(projectMember));

        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void updateProject(ProjectDto projectDto) {
        Project project = projectRepository.findById(projectDto.getId()).orElse(null);

        Account account = accountRepository.findById(projectDto.getProjectManagerId()).orElse(null);

        project.setProjectName(projectDto.getProjectName());
        project.setAccount(account);
        project.setIsActive(projectDto.getIsActive());
        project.setIsAutoActive(projectDto.getIsAutoActive());
        project.setIsDicAbbrUsed(projectDto.getIsDicAbbrUsed());

    }

    @Override
    @Transactional
    public void deleteProject(Long selectedId, AccountDto accountDto) {
        Project project = projectRepository.findById(selectedId).orElse(null);

        if(project == null){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED, "삭제를 할 수 없습니다.");
        }

        if(project.getProjectMembers().size() > 1){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED, "프로젝트 관리자 외의 멤버가 존재하여 삭제가 불가능합니다.");
        }

        if(!project.getAccount().getId().equals(Long.valueOf(accountDto.getId()))){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED, "프로젝트 관리자 외는 삭제가 불가능합니다.");
        }

        if(project.getId().equals(0L)){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED, "기본 프로젝트는 삭제가 불가능합니다.");
        }

        templateRepository.deleteByProjectId(project.getId());
        projectMemberRepository.deleteAll(project.getProjectMembers());

        projectRepository.delete(project);
    }

    @Override
    public boolean selectLoginProject(Long projectId, String id) {
        Project project = projectRepository.findById(projectId).orElse(null);

        if(project != null){
            return project.getAccount().getId().equals(Long.parseLong(id));
        }
        return false;
    }

    @Override
    @Transactional
    public void saveActiveProject(List<ProjectMemberDto> projectMemberDtos, Boolean isActive) {
        if(!AuthUtil.isApprovalAvailable()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        for(ProjectMemberDto projectMemberDto : projectMemberDtos){
            ProjectMember projectMember = projectMemberRepository.findById(projectMemberDto.getId()).orElse(null);

            if(projectMember != null){
                projectMember.setIsActive(isActive);
            }
        }
    }

    @Override
    @Transactional
    public void deleteProjectMember(List<ProjectMemberDto> projectMemberDtos, AccountDto accountDto) {
        if(!AuthUtil.isApprovalAvailable()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        for(ProjectMemberDto projectMemberDto : projectMemberDtos){
            ProjectMember projectMember = projectMemberRepository.findById(projectMemberDto.getId()).orElse(null);

            if(projectMember != null){
                // 프로젝트의 관리자의 경우에는 삭제가 불가능하다.
                if(projectMember.getAccount().getId().equals(Long.parseLong(accountDto.getId()))){
                    throw new CustomException(ErrorCode.MANAGER_NON_DELETED);
                }

                projectMemberRepository.delete(projectMember);
            }
        }
    }

    @Override
    public List<AccountDto> selectProjectMember(Long selectedId, AccountDto accountDto) {

        List<ProjectMember> projectMembers = projectMemberRepository.findAllByProject_Id(selectedId);

        Set<Long> memberAccountIds = projectMembers.stream()
                .map(pm -> pm.getAccount().getId())
                .collect(Collectors.toSet());

        List<AccountDto> allAccount = accountRepository.findAll().stream().map(AccountDto::fromEntity).toList();

        return allAccount.stream()
                .filter(account -> !memberAccountIds.contains(Long.valueOf(account.getId())))
                .toList();
    }

    @Override
    @Transactional
    public void saveProjectMember(Map<String, String> accountIds, Long projectId) {
        if(!AuthUtil.isApprovalAvailable()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        Project project = projectRepository.findById(projectId).orElse(null);

        Account account = accountRepository.findById(Long.valueOf(accountIds.get("accountId"))).orElse(null);

        ProjectMember projectMember = ProjectMember.builder()
                .account(account)
                .project(project)
                .isActive(true)
                .build();

        if(project != null) project.getProjectMembers().add(projectMember);

        projectMemberRepository.save(projectMember);
    }
}
