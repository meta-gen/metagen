package com.koboolean.metagen.system.project.domain.dto;

import com.koboolean.metagen.system.project.domain.entity.Project;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProjectDto {

    private Long id;

    private String projectName;

    private Boolean isActive;

    public static ProjectDto fromEntity(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getProjectName(),
                project.getIsActive()
        );
    }
}
