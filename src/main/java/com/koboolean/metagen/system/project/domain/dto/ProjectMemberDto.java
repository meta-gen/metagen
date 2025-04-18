package com.koboolean.metagen.system.project.domain.dto;

import com.koboolean.metagen.system.project.domain.entity.ProjectMember;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberDto {

    private Long id;
    private Long projectId;
    private String projectName;
    private String username;
    private String name;
    private String isActive;
    private Long accountId;
    private String projectManagerYn;

    public static ProjectMemberDto fromEntity(ProjectMember projectMember) {

        return ProjectMemberDto.builder()
                .id(projectMember.getId())
                .projectId(projectMember.getProject().getId())
                .projectName(projectMember.getProject().getProjectName())
                .username(projectMember.getAccount() != null ? projectMember.getAccount().getUsername() : "")
                .name(projectMember.getAccount() != null ? projectMember.getAccount().getName() : "")
                .isActive(projectMember.getIsActive() ? "Y" : "N")
                .accountId(projectMember.getAccount() != null ? projectMember.getAccount().getId() : null)
                .projectManagerYn(projectMember.getProject() != null ? projectMember.getProject().getAccount().getId().equals(projectMember.getAccount().getId()) ? "O" : "" : null)
                .build();
    }

}
