package com.koboolean.metagen.data.dictionary.service;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.logs.domain.dto.LogsDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DataDictionaryService {
    List<ColumnDto> getStandardTermsColumn();
    List<ColumnDto> getStandardWordsColumn();
    List<ColumnDto> getStandardDomainsColumn();

    Page<StandardTermDto> getStandardTermsData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery);
    Page<StandardWordDto> getStandardWordsData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery);
    Page<StandardDomainDto> getStandardDomainsData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery);

    void uploadDictionaryExcelFile(MultipartFile file, AccountDto accountDto) throws IOException;

    void approvalStandardDomains(List<StandardDomainDto> standardDomains, AccountDto accountDto, boolean isApprovalAvailable);

    void approvalStandardTerms(List<StandardTermDto> standardTerms, AccountDto accountDto, boolean isApprovalAvailable);

    void approvalStandardWords(List<StandardWordDto> standardWords, AccountDto accountDto, boolean isApprovalAvailable);

    void deleteDataDictionaryStandardDomains(List<StandardDomainDto> standardDomains, AccountDto accountDto);
    void deleteDataDictionaryStandardTerms(List<StandardTermDto> standardTerms, AccountDto accountDto);
    void deleteDataDictionaryStandardWords(List<StandardWordDto> standardWords, AccountDto accountDto);

    Map<String, Object> getStandardTermList(String termNm, AccountDto accountDto);

    void insertStandardTerms(AccountDto accountDto, StandardTermDto standardTermDto);
    void updateStandardTerms(AccountDto accountDto, StandardTermDto standardTermDto);

    void insertStandardDomain(AccountDto accountDto, StandardDomainDto standardDomainDto);
    void updateStandardDomain(AccountDto accountDto, StandardDomainDto standardDomainDto);

    void updateStandardWords(AccountDto accountDto, StandardWordDto standardWordDto);
    void insertStandardWord(AccountDto accountDto, StandardWordDto standardWordDto);
}
