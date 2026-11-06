package com.koboolean.metagen.design.code.service;

import com.koboolean.metagen.design.code.domain.dto.CodeRuleDetailDto;
import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.design.rule.domain.dto.CodeRuleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CodeRuleService {
    List<ColumnDto> selectCodeRuleColumn();

    Page<CodeRuleDetailDto> selectCodeRuleData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery, Long tableId);

    List<CodeRuleDto> selectCodeRuleDetailById(AccountDto accountDto, Long codeRuleId);

    CodeRuleDetailDto selectCodeRuleDetail(AccountDto accountDto, CodeRuleDetailDto codeRuleDetailDto);

    void insertCodeRule(AccountDto accountDto, CodeRuleDetailDto codeRuleDetailDto);

    void updateCodeRule(AccountDto accountDto, CodeRuleDetailDto codeRuleDetailDto);

    CodeRuleDetailDto selectCodeRuleDetailDataById(AccountDto accountDto, Long id);

    void deleteCodeRule(AccountDto accountDto, List<CodeRuleDetailDto> dtos);
}
