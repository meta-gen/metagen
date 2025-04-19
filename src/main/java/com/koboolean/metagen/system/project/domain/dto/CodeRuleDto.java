package com.koboolean.metagen.system.project.domain.dto;

import com.koboolean.metagen.system.project.domain.entity.CodeRule;
import com.koboolean.metagen.system.project.domain.entity.Template;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeRuleDto {

    private Long id;

    private Long projectId;

    private String codeRuleName;

    private String codeRuleDescription;

    private TemplateDto template;

    private String templateName;

    private String templateDescription;

    private String prefix;

    private String suffix;

    private String methodForm;

    private String isApproval;

    public static CodeRuleDto fromEntity(CodeRule codeRule) {

        TemplateDto templateDto = TemplateDto.fromEntity(codeRule.getTemplate());

        return builder()
                .id(codeRule.getId())
                .projectId(codeRule.getProjectId())
                .codeRuleName(codeRule.getCodeRuleName())
                .codeRuleDescription(codeRule.getCodeRuleDescription())
                .template(templateDto)
                .templateName(templateDto.getTemplateName())
                .templateDescription(templateDto.getTemplateDescription())
                .prefix(codeRule.getPrefix())
                .suffix(codeRule.getSuffix())
                .methodForm(codeRule.getMethodForm())
                .isApproval(codeRule.getIsApproval() ? "Y" : "N")
                .build();
    }
}
