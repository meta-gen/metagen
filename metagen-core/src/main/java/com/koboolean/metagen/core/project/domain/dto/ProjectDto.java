package com.koboolean.metagen.core.project.domain.dto;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.core.project.domain.enums.DbType;
import com.koboolean.metagen.core.util.AuthUtil;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

    private Long id;

    private String projectName;

    private String projectManagerName;

    private Long projectManagerId;

    private Boolean isActive;

    private Boolean isAutoActive;

    private List<ProjectMemberDto> projectMembers;

    private Boolean isModified;

    private Boolean isDicAbbrUsed;

    private Boolean isSelected;

    /** 테이블명 접두어 (예: TB_) */
    private String tablePrefix;

    /** 대상 DB (DbType 이름 : POSTGRESQL / ORACLE / MYSQL / MARIADB) - 테이블 DDL 생성 기준 */
    private String dbType;

    public static ProjectDto fromEntity(Project project) {

        List<ProjectMemberDto> projectMembers = project.getProjectMember().stream().map(ProjectMemberDto::fromEntity).toList();

        return new ProjectDto(
                project.getId(),
                project.getProjectName(),
                project.getAccount() != null ? project.getAccount().getName() : null,
                project.getAccount() != null ? project.getAccount().getId() : null,
                project.getIsActive(),
                project.getIsAutoActive(),
                projectMembers,
                false,
                project.getIsDicAbbrUsed(),
                false,
                project.getTablePrefix(),
                DbType.orDefault(project.getDbType()).name()
        );
    }

    public static ProjectDto fromEntity(Project project, AccountDto accountDto) {

        List<ProjectMemberDto> projectMembers = project.getProjectMember().stream().map(ProjectMemberDto::fromEntity).toList();
        Boolean isModified = project.getAccount() != null && (project.getAccount().getId() == Long.parseLong(accountDto.getId())
                || AuthUtil.isApprovalAvailable());

        return new ProjectDto(
                project.getId(),
                project.getProjectName(),
                project.getAccount() != null ? project.getAccount().getName() : null,
                project.getAccount() != null ? project.getAccount().getId() : null,
                project.getIsActive(),
                project.getIsAutoActive(),
                projectMembers,
                isModified,
                project.getIsDicAbbrUsed(),
                false,
                project.getTablePrefix(),
                DbType.orDefault(project.getDbType()).name()
        );
    }
}
