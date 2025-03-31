package com.koboolean.metagen.data.dictionary.repository;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

@Repository
public interface StandardWordRepository extends JpaRepository<StandardWord, Long> {
    Page<StandardWord> findAllByProjectId(Long projectId, Pageable pageable);
    Page<StandardWord> findByRevisionNumberContainingAndProjectId(int revisionNumber, Long projectId, Pageable pageable);
    Page<StandardWord> findByCommonStandardWordNameContainingAndProjectId(String commonStandardWordName, Long projectId, Pageable pageable);
    Page<StandardWord> findByCommonStandardWordAbbreviationContainingAndProjectId(String commonStandardWordAbbreviation, Long projectId, Pageable pageable);
    Page<StandardWord> findByCommonStandardWordEnglishNameContainingAndProjectId(String commonStandardWordEnglishName, Long projectId, Pageable pageable);
    Page<StandardWord> findByCommonStandardWordDescriptionContainingAndProjectId(String commonStandardWordDescription, Long projectId, Pageable pageable);
    Page<StandardWord> findByIsFormatWordAndProjectId(boolean isFormatWord, Long projectId, Pageable pageable);
    Page<StandardWord> findByCommonStandardDomainCategoryContainingAndProjectId(String commonStandardDomainCategory, Long projectId, Pageable pageable);
    Page<StandardWord> findBySynonymListContainingAndProjectId(String synonym, Long projectId, Pageable pageable);
    Page<StandardWord> findByRestrictedWordsContainingAndProjectId(String restrictedWord, Long projectId, Pageable pageable);
    Page<StandardWord> findByIsApprovalAndProjectId(Boolean isApproval, Long projectId, Pageable pageable);
    Page<StandardWord> findAllByIdAndProjectId(Long searchQuery, Long projectId, Pageable pageable);

    StandardWord findByIdAndProjectId(Long id, Long projectId);

    StandardWord findByCommonStandardWordAbbreviationAndProjectId(String commonStandardWordAbbreviation, Long projectId);

    StandardWord findBycommonStandardWordName(String commonStandardWordName);

    StandardWord findByCommonStandardWordNameAndProjectId(String s, Long projectId);
}
