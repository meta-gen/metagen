package com.koboolean.metagen.data.dictionary.repository;

import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Repository
public interface StandardTermRepository extends JpaRepository<StandardTerm, Long> {

    Page<StandardTerm> findAllByProjectId(Long projectId, Pageable pageable);
    Page<StandardTerm> findAllByIdAndProjectId(Long searchQuery, Long projectId, Pageable pageable);

    Page<StandardTerm> findByRevisionNumberAndProjectId(int revisionNumber, Long projectId, Pageable pageable);
    Page<StandardTerm> findByCommonStandardTermNameContainingAndProjectId(String name, Long projectId, Pageable pageable);
    Page<StandardTerm> findByCommonStandardTermDescriptionContainingAndProjectId(String desc, Long projectId, Pageable pageable);
    Page<StandardTerm> findByCommonStandardTermAbbreviationContainingAndProjectId(String abbr, Long projectId, Pageable pageable);
    Page<StandardTerm> findByStandardDomain_CommonStandardDomainNameContainingAndProjectId(String domainName, Long projectId, Pageable pageable);
    Page<StandardTerm> findByStandardDomain_StorageFormatContainingAndProjectId(String storageFormat, Long projectId, Pageable pageable);
    Page<StandardTerm> findByAdministrativeStandardCodeNameContainingAndProjectId(String code, Long projectId, Pageable pageable);
    Page<StandardTerm> findByResponsibleOrganizationContainingAndProjectId(String org, Long projectId, Pageable pageable);
    Page<StandardTerm> findBySynonymListContainingAndProjectId(String synonym, Long projectId, Pageable pageable);
    Page<StandardTerm> findByIsApprovalAndProjectId(Boolean isApproval, Long projectId, Pageable pageable);
    Page<StandardTerm> findByStandardDomain_AllowedValuesContainingAndProjectId(String searchQuery, Long projectId, Pageable pageable);
    Page<StandardTerm> findByStandardDomain_DisplayFormatContainingAndProjectId(String searchQuery, Long projectId, Pageable pageable);

    // 단건 조회
    StandardTerm findByIdAndProjectId(Long id, Long projectId);

    // 중복 체크
    List<StandardTerm> findByCommonStandardTermAbbreviationAndStandardDomain_CommonStandardDomainNameAndProjectId(String abbr, String domainName, Long projectId);


    List<StandardTerm> findAllByProjectIdAndCommonStandardTermAbbreviationContaining(Long projectId, String commonStandardTermAbbreviation);

    List<StandardTerm> findAllByProjectIdAndCommonStandardTermNameContaining(Long projectId, String commonStandardTermName);
}
