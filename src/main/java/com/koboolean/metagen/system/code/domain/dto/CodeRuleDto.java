package com.koboolean.metagen.system.code.domain.dto;

import com.koboolean.metagen.system.code.domain.entity.CodeRule;
import com.koboolean.metagen.system.project.domain.dto.TemplateDto;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeRuleDto {

    private Long id;

    private Long projectId;

    private Long templateId;

    private String codeRuleName;

    private String codeRuleDescription;

    private TemplateDto template;

    private String templateName;

    private String templateDescription;

    private String prefix;

    private String suffix;

    private String methodForm;

    private String isApproval;

    private String input;

    private String output;

    public static CodeRuleDto fromEntity(CodeRule codeRule) {

        TemplateDto templateDto = TemplateDto.fromEntity(codeRule.getTemplate());

        return builder()
                .id(codeRule.getId())
                .projectId(codeRule.getProjectId())
                .templateId(templateDto.getId())
                .codeRuleName(codeRule.getCodeRuleName())
                .codeRuleDescription(codeRule.getCodeRuleDescription())
                .template(templateDto)
                .templateName(templateDto.getTemplateName())
                .templateDescription(templateDto.getTemplateDescription())
                .prefix(codeRule.getPrefix())
                .suffix(codeRule.getSuffix())
                .methodForm(codeRule.getMethodForm())
                .input(codeRule.getInput())
                .output(codeRule.getOutput())
                .build();
    }
}
