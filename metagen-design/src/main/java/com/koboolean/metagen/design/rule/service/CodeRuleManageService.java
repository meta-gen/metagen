package com.koboolean.metagen.design.rule.service;

import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.design.rule.domain.dto.CodeRuleDto;
import com.koboolean.metagen.design.rule.domain.dto.TemplateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CodeRuleManageService {
    void saveCodeRuleManageTemplate(Long projectId, TemplateDto template);

    List<TemplateDto> selectCodeRuleManageTemplate(Long projectId);

    void deleteCodeRuleManageTemplate(Long selectedId);

    List<ColumnDto> getCodeRuleManageColumn();

    Page<CodeRuleDto> getCodeRuleManageData(Pageable pageable, String searchColumn, String searchQuery, Long projectId);

    void saveCodeRuleManage(CodeRuleDto codeRuleDto, AccountDto accountDto);

    void deleteCodeRuleManage(Long projectId, List<CodeRuleDto> codeRuleDtos);

    CodeRuleDto selectCodeRuleManageDetail(Long projectId, Long id);

    void updateCodeRuleManage(CodeRuleDto codeRuleDto, AccountDto accountDto);
}
