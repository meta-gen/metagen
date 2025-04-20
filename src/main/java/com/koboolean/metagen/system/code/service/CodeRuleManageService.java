package com.koboolean.metagen.system.code.service;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.project.domain.dto.CodeRuleDto;
import com.koboolean.metagen.system.project.domain.dto.TemplateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CodeRuleManageService {
    void saveCodeRuleManageTemplate(Long projectId, TemplateDto template);

    List<TemplateDto> selectCodeRuleManageTemplate(Long projectId);

    void deleteCodeRuleManageTemplate(Long selectedId);

    List<ColumnDto> getCodeRuleManageColumn();

    Page<CodeRuleDto> getCodeRuleManageData(Pageable pageable, String searchColumn, String searchQuery, Long projectId);

    void saveCodeRuleManage(CodeRuleDto codeRuleDto);
}
