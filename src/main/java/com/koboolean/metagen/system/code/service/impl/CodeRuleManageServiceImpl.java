package com.koboolean.metagen.system.code.service.impl;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.system.code.repository.TemplateRepository;
import com.koboolean.metagen.system.code.service.CodeRuleManageService;
import com.koboolean.metagen.system.project.domain.dto.TemplateDto;
import com.koboolean.metagen.system.project.domain.entity.Project;
import com.koboolean.metagen.system.project.domain.entity.Template;
import com.koboolean.metagen.system.project.repository.ProjectRepository;
import com.koboolean.metagen.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeRuleManageServiceImpl implements CodeRuleManageService {

    private final ProjectRepository projectRepository;
    private final TemplateRepository templateRepository;

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
}
