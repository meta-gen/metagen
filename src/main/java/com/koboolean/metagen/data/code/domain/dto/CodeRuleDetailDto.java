package com.koboolean.metagen.data.code.domain.dto;

import com.koboolean.metagen.data.code.domain.entity.CodeRuleDetail;
import com.koboolean.metagen.system.code.domain.dto.CodeRuleDto;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeRuleDetailDto {

    private Long id;

    private Long projectId;

    private Long templateId;

    private String templateName;

    private Long codeRuleId;

    private String codeRuleName; // 코드 규칙 명

    private String functionGroup; // 그룹명 (ex: 회원관리, 게시판 등)

    private String methodKeyword;   // ${1} - 사용자 입력 기반, 표준용어 조합 키워드

    private String methodPurpose;   // ${2} - 메소드의 기능이나 목적 설명

    private String description; // 기타 설명

    private String methodName; // ${0} 변환된 메소드 명

    private String swaggerData; // 스웨거 변환 데이터

    private Boolean isDicAbbrUsed;  // true면 약어 기준, false면 영문명 기준

    private CodeRuleDto codeRuleDto; // 코드규칙

    private String methodForm;

    private Boolean useSwagger;

    private String input;

    private String output;

    private String exception;

    public static CodeRuleDetailDto fromEntity(CodeRuleDetail entity) {
        return builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .templateId(entity.getCodeRule().getTemplate().getId())
                .templateName(entity.getCodeRule().getTemplate().getTemplateName())
                .codeRuleId(entity.getCodeRule().getId())
                .codeRuleName(entity.getCodeRule().getCodeRuleName())
                .functionGroup(entity.getFunctionGroup())
                .description(entity.getDescription())
                .methodName(entity.getMethodName())
                .swaggerData(entity.getSwaggerData())
                .methodKeyword(entity.getMethodKeyword())
                .methodPurpose(entity.getMethodPurpose())
                .isDicAbbrUsed(entity.getIsDicAbbrUsed())
                .codeRuleDto(CodeRuleDto.fromEntity(entity.getCodeRule()))
                .useSwagger(entity.getUseSwagger())
                .input(entity.getInput())
                .output(entity.getOutput())
                .exception(entity.getException())
                .build();
    }

}
