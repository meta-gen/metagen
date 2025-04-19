package com.koboolean.metagen.system.code.service;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.project.domain.dto.TemplateDto;

import java.util.List;

public interface CodeRuleManageService {
    void saveCodeRuleManageTemplate(Long projectId, TemplateDto template);

    List<TemplateDto> selectCodeRuleManageTemplate(Long projectId);

    void deleteCodeRuleManageTemplate(Long selectedId);
}
