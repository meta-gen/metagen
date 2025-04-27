package com.koboolean.metagen.system.project.domain.dto;

import com.koboolean.metagen.system.project.domain.entity.Template;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDto {

    private Long id;

    private Long projectId;

    private String templateName;

    private String templateDescription;

    private ProjectDto project;

    public static TemplateDto fromEntity(Template template) {
        return builder()
                .id(template.getId())
                .projectId(template.getProjectId())
                .templateName(template.getTemplateName())
                .templateDescription(template.getTemplateDescription())
                .project(ProjectDto.fromEntity(template.getProject()))
                .build();
    }
}
