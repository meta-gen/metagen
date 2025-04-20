package com.koboolean.metagen.data.dictionary.repository;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Repository
public interface StandardDomainRepository extends JpaRepository<StandardDomain, Long> {
    Page<StandardDomain> findAllByProjectId(Long projectId, Pageable pageable);

    Page<StandardDomain> findByRevisionNumberContainingAndProjectId(String keyword, Long projectId, Pageable pageable);
    Page<StandardDomain> findByCommonStandardDomainGroupNameContainingAndProjectId(String keyword, Long projectId, Pageable pageable);
    Page<StandardDomain> findByCommonStandardDomainCategoryContainingAndProjectId(String keyword, Long projectId, Pageable pageable);
    Page<StandardDomain> findByCommonStandardDomainNameContainingAndProjectId(String keyword, Long projectId, Pageable pageable);
    Page<StandardDomain> findByCommonStandardDomainDescriptionContainingAndProjectId(String keyword, Long projectId, Pageable pageable);
    Page<StandardDomain> findByDataTypeContainingAndProjectId(String keyword, Long projectId, Pageable pageable);
    Page<StandardDomain> findByDataLengthContainingAndProjectId(String keyword, Long projectId, Pageable pageable);
    Page<StandardDomain> findByDataDecimalLengthContainingAndProjectId(String keyword, Long projectId, Pageable pageable);
    Page<StandardDomain> findByStorageFormatContainingAndProjectId(String keyword, Long projectId, Pageable pageable);
    Page<StandardDomain> findByDisplayFormatContainingAndProjectId(String keyword, Long projectId, Pageable pageable);
    Page<StandardDomain> findByUnitContainingAndProjectId(String keyword, Long projectId, Pageable pageable);
    Page<StandardDomain> findByAllowedValuesContainingAndProjectId(String keyword, Long projectId, Pageable pageable);
    Page<StandardDomain> findByIsApprovalAndProjectId(Boolean isApproval, Long projectId, Pageable pageable);
    Page<StandardDomain> findAllByIdAndProjectId(Long searchQuery, Long projectId, Pageable pageable);

    StandardDomain findByIdAndProjectId(Long id, Long projectId);

    List<StandardDomain> findAllByCommonStandardDomainCategoryAndProjectId(String splitDatum, Long projectId);

    StandardDomain findByCommonStandardDomainNameAndProjectId(String domainName, Long projectId);

    List<StandardDomain> findAllByCommonStandardDomainCategoryAndCommonStandardDomainCategoryAndProjectId(String commonStandardDomainCategory, String commonStandardDomainCategory1, Long projectId);

    List<StandardDomain> findAllByProjectIdAndCommonStandardDomainCategory(Long projectId, String commonStandardDomainCategory);
}
