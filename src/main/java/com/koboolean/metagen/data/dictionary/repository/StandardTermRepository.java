package com.koboolean.metagen.data.dictionary.repository;

import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

@RestController
public interface StandardTermRepository extends JpaRepository<StandardTerm, Long> {
    Page<StandardTerm> findAllByProjectId(Long projectId, Pageable pageable);

    Page<StandardTerm> findByRevisionNumberAndProjectId(int revisionNumber, Long projectId, Pageable pageable);
    Page<StandardTerm> findByCommonStandardTermNameContainingAndProjectId(String commonStandardTermName, Long projectId, Pageable pageable);
    Page<StandardTerm> findByCommonStandardTermDescriptionContainingAndProjectId(String commonStandardTermDescription, Long projectId, Pageable pageable);
    Page<StandardTerm> findByCommonStandardTermAbbreviationContainingAndProjectId(String commonStandardTermAbbreviation, Long projectId, Pageable pageable);
    Page<StandardTerm> findByCommonStandardDomainNameContainingAndProjectId(String commonStandardDomainName, Long projectId, Pageable pageable);
    Page<StandardTerm> findByAllowedValuesContainingAndProjectId(String allowedValues, Long projectId, Pageable pageable);
    Page<StandardTerm> findByStorageFormatContainingAndProjectId(String storageFormat, Long projectId, Pageable pageable);
    Page<StandardTerm> findByDisplayFormatContainingAndProjectId(String displayFormat, Long projectId, Pageable pageable);
    Page<StandardTerm> findByAdministrativeStandardCodeNameContainingAndProjectId(String administrativeStandardCodeName, Long projectId, Pageable pageable);
    Page<StandardTerm> findByResponsibleOrganizationContainingAndProjectId(String responsibleOrganization, Long projectId, Pageable pageable);
    Page<StandardTerm> findBySynonymListContainingAndProjectId(String synonym, Long projectId, Pageable pageable);
    Page<StandardTerm> findByIsApprovalAndProjectId(Boolean isApproval, Long projectId, Pageable pageable);
    Page<StandardTerm> findAllByIdAndProjectId(Long searchQuery, Long projectId, Pageable pageable);
}
