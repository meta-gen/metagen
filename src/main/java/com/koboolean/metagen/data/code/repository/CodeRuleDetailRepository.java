package com.koboolean.metagen.data.code.repository;

import com.koboolean.metagen.data.code.domain.entity.CodeRuleDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeRuleDetailRepository extends JpaRepository<CodeRuleDetail, Long> {
    Page<CodeRuleDetail> findByProjectId(Long projectId, Pageable pageable);

    Page<CodeRuleDetail> findAllByProjectIdAndFunctionGroupLike(Long projectId, String functionGroup, Pageable pageable);

    List<CodeRuleDetail> findAllByProjectIdAndFunctionGroupLike(Long projectId, String functionGroup);

    Page<CodeRuleDetail> findAllByProjectIdAndMethodKeywordLike(Long projectId, String methodKeyword, Pageable pageable);

    List<CodeRuleDetail> findAllByProjectIdAndMethodKeywordLike(Long projectId, String methodKeyword);

    CodeRuleDetail findByProjectIdAndFunctionGroupAndMethodKeywordAndCodeRule_Id(Long projectId, String functionGroup, String methodKeyword, Long codeRuleId);

    CodeRuleDetail findByIdAndProjectId(Long id, Long projectId);

    Page<CodeRuleDetail> findAllByProjectIdAndCodeRule_CodeRuleNameLike(Long projectId, String codeRuleCodeRuleName, Pageable pageable);

    Page<CodeRuleDetail> findAllByProjectIdAndCodeRule_Template_TemplateNameLike(Long projectId, String codeRuleTemplateTemplateName, Pageable pageable);

    List<CodeRuleDetail> findAllByProjectIdAndCodeRule_CodeRuleNameLike(Long projectId, String codeRuleCodeRuleName);

    List<CodeRuleDetail> findAllByProjectIdAndCodeRule_Template_TemplateNameLike(Long projectId, String codeRuleTemplateTemplateName);

    List<CodeRuleDetail> findAllByProjectId(Long projectId);
}
