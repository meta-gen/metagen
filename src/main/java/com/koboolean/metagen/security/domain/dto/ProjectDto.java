package com.koboolean.metagen.security.domain.dto;

import com.koboolean.metagen.security.domain.entity.Project;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProjectDto {

    private Long id;

    private String projectName;

    public static ProjectDto fromEntity(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getProjectName()
        );
    }
}
