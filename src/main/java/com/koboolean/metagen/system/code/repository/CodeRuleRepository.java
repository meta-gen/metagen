package com.koboolean.metagen.system.code.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.koboolean.metagen.system.project.domain.entity.CodeRule;
import com.koboolean.metagen.system.project.domain.entity.ProjectMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeRuleRepository extends JpaRepository<CodeRule, Long> {
    Page<CodeRule> findAllByProjectId(Long projectId, Pageable pageable);

    Page<CodeRule> findAllByProjectIdAndTemplate_TemplateNameLike(Long projectId, String searchQuery, Pageable pageable);

    Page<CodeRule> findAllByProjectIdAndCodeRuleNameLike(Long projectId, String codeRuleName, Pageable pageable);

    List<CodeRule> findAllByProjectIdAndCodeRuleNameAndTemplate_Id(Long projectId, String codeRuleName, Long templateId);
}
