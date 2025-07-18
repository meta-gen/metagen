package com.koboolean.metagen.system.project.domain.dto;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.project.domain.entity.Project;
import com.koboolean.metagen.system.project.domain.entity.Template;
import com.koboolean.metagen.utils.AuthUtil;
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

    private List<TemplateDto> templates;

    private Boolean isSelected;

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
                project.getTemplateType().stream().map(TemplateDto::fromEntity).toList(),
                false
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
                project.getTemplateType().stream().map(TemplateDto::fromEntity).toList(),
                false
        );
    }
}
