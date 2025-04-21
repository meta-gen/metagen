package com.koboolean.metagen.system.code.service.impl;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.system.code.repository.CodeRuleRepository;
import com.koboolean.metagen.system.code.repository.TemplateRepository;
import com.koboolean.metagen.system.code.service.CodeRuleManageService;
import com.koboolean.metagen.system.project.domain.dto.CodeRuleDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.system.project.domain.dto.TemplateDto;
import com.koboolean.metagen.system.project.domain.entity.CodeRule;
import com.koboolean.metagen.system.project.domain.entity.Project;
import com.koboolean.metagen.system.project.domain.entity.ProjectMember;
import com.koboolean.metagen.system.project.domain.entity.Template;
import com.koboolean.metagen.system.project.repository.ProjectRepository;
import com.koboolean.metagen.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CodeRuleManageServiceImpl implements CodeRuleManageService {

    private final ProjectRepository projectRepository;
    private final TemplateRepository templateRepository;
    private final CodeRuleRepository codeRuleRepository;

    @Override
    @Transactional
    public void saveCodeRuleManageTemplate(Long projectId, TemplateDto template) {
        if(!AuthUtil.isApprovalAdmin()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        Project project = projectRepository.findById(projectId).orElse(null);

        if (project == null) {
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }
        Template templateEntity = Template.builder()
                .projectId(projectId)
                .templateName(template.getTemplateName())
                .templateDescription(template.getTemplateDescription())
                .project(project)
                .build();

        List<Template> templates = templateRepository.findAllByProjectIdAndTemplateName(projectId, template.getTemplateName());

        if(!templates.isEmpty()){
            throw new CustomException(ErrorCode.TEMPLATE_NAME_IN_PROJECT_DUPLICATE);
        }

        templateRepository.save(templateEntity);
    }

    @Override
    public List<TemplateDto> selectCodeRuleManageTemplate(Long projectId) {
        if(!AuthUtil.isApprovalAdmin()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        return templateRepository.findAllByProjectId(projectId).stream().map(TemplateDto::fromEntity).toList();
    }

    @Override
    @Transactional
    public void deleteCodeRuleManageTemplate(Long selectedId) {
        if(!AuthUtil.isApprovalAdmin()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        Template template = templateRepository.findById(selectedId).orElse(null);
        if(template == null){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        //TODO: 템플릿 내부 데이터가 존재하는지 확인 후 제거를 수행하여야한다.
        templateRepository.deleteById(selectedId);
    }

    @Override
    public List<ColumnDto> getCodeRuleManageColumn() {
        return List.of(new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX, false)
                , new ColumnDto("템플릿 명", "templateName", ColumnType.STRING, RowType.TEXT, true, true)
                , new ColumnDto("코드규칙 명", "codeRuleName", ColumnType.STRING, RowType.TEXT, true, true)
                , new ColumnDto("코드규칙 내용", "codeRuleDescription", ColumnType.STRING, RowType.TEXT, false)
                , new ColumnDto("접두사", "prefix", ColumnType.STRING, RowType.TEXT, false)
                , new ColumnDto("접미사", "suffix", ColumnType.STRING, RowType.TEXT, false));
    }

    @Override

    public Page<CodeRuleDto> getCodeRuleManageData(Pageable pageable, String searchColumn, String searchQuery, Long projectId) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // 검색어가 없을 경우 전체 조회
            Page<CodeRule> allByProjectId = codeRuleRepository.findAllByProjectId(projectId, pageable);

            return allByProjectId.map(CodeRuleDto::fromEntity);
        }

        searchQuery = "%" + searchQuery + "%";

        Page<CodeRule> codeRules = switch (searchColumn) {
            case "templateName" ->
                    codeRuleRepository.findAllByProjectIdAndTemplate_TemplateNameLike(projectId, searchQuery, pageable);
            case "codeRuleName" ->
                    codeRuleRepository.findAllByProjectIdAndCodeRuleNameLike(projectId, searchQuery, pageable);
            default -> null;
        };

        return codeRules.map(CodeRuleDto::fromEntity);
    }

    @Override
    @Transactional
    public void saveCodeRuleManage(CodeRuleDto codeRuleDto) {
        if(!AuthUtil.isApprovalAdmin()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        CodeRule codeRule = CodeRule.builder()
                .projectId(codeRuleDto.getProjectId())
                .codeRuleName(codeRuleDto.getCodeRuleName())
                .codeRuleDescription(codeRuleDto.getCodeRuleDescription())
                .prefix(codeRuleDto.getPrefix())
                .suffix(codeRuleDto.getSuffix())
                .methodForm(codeRuleDto.getMethodForm())
                .build();

        Template template = templateRepository.findById(codeRuleDto.getTemplateId()).orElse(null);
        Project project = projectRepository.findById(codeRuleDto.getProjectId()).orElse(null);

        if(template == null || project == null){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        codeRule.setTemplate(template);
        codeRule.setTemplateName(template.getTemplateName());

        List<CodeRule> isCodeDupl = codeRuleRepository.findAllByProjectIdAndCodeRuleNameAndTemplate_Id(codeRuleDto.getProjectId(), codeRuleDto.getCodeRuleName(), template.getId());

        if(!isCodeDupl.isEmpty()){
            throw new CustomException(ErrorCode.TEMPLATE_CODE_RULE_NAME_IN_PROJECT_DUPLICATE);
        }

        codeRuleRepository.save(codeRule);

        template.getCodeRules().add(codeRule);
    }

    @Override
    @Transactional
    public void deleteCodeRuleManage(Long projectId, List<CodeRuleDto> codeRuleDtos) {
        if(!AuthUtil.isApprovalAdmin()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        codeRuleDtos.forEach(codeRuleDto -> {

            CodeRule codeRule = codeRuleRepository.findById(codeRuleDto.getId()).orElse(null);

            if(codeRule != null){
                Template template = templateRepository.findById(codeRule.getTemplate().getId()).orElse(null);

                Objects.requireNonNull(template).getCodeRules().remove(codeRule);

                codeRuleRepository.delete(codeRule);
            }

        });
    }

    @Override
    public CodeRuleDto selectCodeRuleManageDetail(Long projectId, Long id) {
        CodeRule codeRule = codeRuleRepository.findByIdAndProjectId(id, projectId);

        if(codeRule == null){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        return CodeRuleDto.fromEntity(codeRule);
    }

    @Override
    @Transactional
    public void updateCodeRuleManage(CodeRuleDto codeRuleDto) {
        if(!AuthUtil.isApprovalAdmin()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        CodeRule codeRule = codeRuleRepository.findById(codeRuleDto.getId()).orElse(null);

        if(codeRule == null){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        codeRule.setCodeRuleName(codeRuleDto.getCodeRuleName());
        codeRule.setCodeRuleDescription(codeRuleDto.getCodeRuleDescription());
        codeRule.setPrefix(codeRuleDto.getPrefix());
        codeRule.setSuffix(codeRuleDto.getSuffix());
        codeRule.setMethodForm(codeRuleDto.getMethodForm());

        Template template = templateRepository.findById(codeRuleDto.getTemplateId()).orElse(null);

        if(template == null){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        codeRule.setTemplate(template);

        template.getCodeRules().remove(codeRule);
        template.getCodeRules().add(codeRule);
    }


}
