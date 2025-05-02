package com.koboolean.metagen.data.dictionary.service.impl;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardDomainRepository;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
import com.koboolean.metagen.data.dictionary.repository.StandardWordRepository;
import com.koboolean.metagen.data.dictionary.service.DataDictionaryService;
import com.koboolean.metagen.data.dictionary.service.StandardDomainService;
import com.koboolean.metagen.data.dictionary.service.StandardTermService;
import com.koboolean.metagen.data.dictionary.service.StandardWordService;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.utils.AuthUtil;
import com.koboolean.metagen.utils.ExcelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DataDictionaryServiceImpl implements DataDictionaryService {

    private final StandardWordService standardWordService;
    private final StandardDomainService standardDomainService;
    private final StandardTermService standardTermService;

    @Override
    public List<ColumnDto> getStandardTermsColumn() {
        return List.of(
                new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX),
                new ColumnDto("제정차수", "revisionNumber", ColumnType.NUMBER),
                new ColumnDto("표준용어명", "commonStandardTermName", ColumnType.STRING, true),
                new ColumnDto("표준용어설명", "commonStandardTermDescription", ColumnType.STRING, true),
                new ColumnDto("표준용어영문약어명", "commonStandardTermAbbreviation", ColumnType.STRING, true),
                new ColumnDto("표준도메인명", "commonStandardDomainName", ColumnType.STRING, true),
                new ColumnDto("허용값", "allowedValues", ColumnType.STRING),
                new ColumnDto("저장 형식", "storageFormat", ColumnType.STRING),
                new ColumnDto("표현 형식", "displayFormat", ColumnType.STRING),
                new ColumnDto("행정표준코드명", "administrativeStandardCodeName", ColumnType.STRING),
                new ColumnDto("소관기관명", "responsibleOrganization", ColumnType.STRING),
                new ColumnDto("용어 이음동의어 목록", "synonyms", ColumnType.STRING, RowType.TEXT, false, false),
                new ColumnDto("승인여부", "isApprovalYn", ColumnType.STRING, RowType.TEXT,false, false)
        );
    }

    @Override
    public List<ColumnDto> getStandardDomainsColumn() {
        return List.of(
                new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX),
                new ColumnDto("제정차수", "revisionNumber", ColumnType.NUMBER),
                new ColumnDto("데이터길이", "dataLength", ColumnType.NUMBER),
                new ColumnDto("데이터소수점길이", "dataDecimalLength", ColumnType.NUMBER),
                new ColumnDto("표준도메인그룹명", "commonStandardDomainGroupName", ColumnType.STRING, true),
                new ColumnDto("표준도메인분류명", "commonStandardDomainCategory", ColumnType.STRING, true),
                new ColumnDto("표준도메인명", "commonStandardDomainName", ColumnType.STRING, true),
                new ColumnDto("표준도메인설명", "commonStandardDomainDescription", ColumnType.STRING, true),
                new ColumnDto("데이터타입", "dataType", ColumnType.STRING),
                new ColumnDto("저장 형식", "storageFormat", ColumnType.STRING),
                new ColumnDto("표현 형식", "displayFormat", ColumnType.STRING),
                new ColumnDto("단위", "unit", ColumnType.STRING),
                new ColumnDto("허용값", "allowedValues", ColumnType.STRING),
                new ColumnDto("승인여부", "isApprovalYn", ColumnType.STRING, RowType.TEXT,false, false)
        );
    }

    @Override
    public List<ColumnDto> getStandardWordsColumn() {
        return List.of(
                new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX),
                new ColumnDto("제정차수", "revisionNumber", ColumnType.NUMBER),
                new ColumnDto("표준단어명", "commonStandardWordName", ColumnType.STRING, true),
                new ColumnDto("표준단어영문약어명", "commonStandardWordAbbreviation", ColumnType.STRING, true),
                new ColumnDto("표준단어 영문명", "commonStandardWordEnglishName", ColumnType.STRING, true),
                new ColumnDto("표준단어 설명", "commonStandardWordDescription", ColumnType.STRING, true),
                new ColumnDto("표준도메인분류명", "commonStandardDomainCategory", ColumnType.STRING, true),
                new ColumnDto("약어사용여부", "useAbbreviation", ColumnType.STRING, RowType.TEXT, false, false),
                new ColumnDto("형식단어 여부", "isFormatWord", ColumnType.STRING, RowType.TEXT, false, false),
                new ColumnDto("이음동의어 목록", "synonyms", ColumnType.STRING, RowType.TEXT, false, false),
                new ColumnDto("금칙어 목록", "restrictedWords", ColumnType.STRING, RowType.TEXT, false, false),
                new ColumnDto("승인여부", "isApprovalYn", ColumnType.STRING, RowType.TEXT, false, false)
        );
    }

    @Override
    public Page<StandardTermDto> getStandardTermsData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery) {
        return standardTermService.getStandardTermsData(pageable, accountDto, searchColumn, searchQuery);
    }

    @Override
    public Page<StandardWordDto> getStandardWordsData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery) {
        return standardWordService.getStandardWordsData(pageable, accountDto, searchColumn, searchQuery);
    }

    @Override
    public Page<StandardDomainDto> getStandardDomainsData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery) {
        return standardDomainService.getStandardDomainsData(pageable, accountDto, searchColumn, searchQuery);
    }

    @Override
    @Transactional
    public void uploadDictionaryExcelFile(MultipartFile file, AccountDto accountDto) throws IOException {

        Long projectId = accountDto.getProjectId();

        // 관리자의 경우 승인으로 저장, 아닐경우 관리자가 승인할 수 있도록 저장
        boolean isApprovalAvailable = AuthUtil.isApprovalAvailable();

        standardDomainService.setStandardDomain(file, projectId, isApprovalAvailable);
        standardWordService.setStandardWord(file, projectId, isApprovalAvailable);
        standardTermService.setStandardTerm(file, projectId, isApprovalAvailable);
    }

    @Override
    @Transactional
    public void approvalStandardDomains(List<StandardDomainDto> standardDomains, AccountDto accountDto, boolean isApprovalAvailable) {
        standardDomainService.approvalStandardDomains(standardDomains, accountDto, isApprovalAvailable);
    }

    @Override
    @Transactional
    public void approvalStandardTerms(List<StandardTermDto> standardTerms, AccountDto accountDto, boolean isApprovalAvailable) {
        standardTermService.approvalStandardTerms(standardTerms, accountDto, isApprovalAvailable);
    }

    @Override
    public void approvalStandardWords(List<StandardWordDto> standardWords, AccountDto accountDto, boolean isApprovalAvailable) {
        standardWordService.approvalStandardWords(standardWords, accountDto, isApprovalAvailable);
    }

    @Override
    @Transactional
    public void deleteDataDictionaryStandardDomains(List<StandardDomainDto> standardDomains, AccountDto accountDto) {
        standardDomainService.deleteDataDictionaryStandardDomains(standardDomains, accountDto);
    }

    @Override
    @Transactional
    public void deleteDataDictionaryStandardTerms(List<StandardTermDto> standardTerms, AccountDto accountDto) {
        standardTermService.deleteDataDictionaryStandardTerms(standardTerms, accountDto);
    }

    @Override
    @Transactional
    public void deleteDataDictionaryStandardWords(List<StandardWordDto> standardWords, AccountDto accountDto) {
        standardWordService.deleteDataDictionaryStandardWords(standardWords, accountDto);
    }

    @Override
    public Map<String, Object> getStandardTermList(String termNm, AccountDto accountDto) {
        Map<String, Object> result = new HashMap<>();

        Long projectId = accountDto.getProjectId();

        List<StandardWordDto> list = new ArrayList<>();

        String[] splitData = termNm.split(" ");

        for (int i = 0; i < splitData.length; i++) {
            StandardWord standardWord = standardWordService.commonStandardWordName(splitData[i], projectId);
            if (standardWord != null) {
                list.add(StandardWordDto.fromEntity(standardWord));
            }

            if(i == splitData.length - 1 && standardWord != null) {
                List<StandardDomain> standardDomains = standardDomainService.getStandardDomains(standardWord.getCommonStandardDomainCategory(), projectId);
                List<StandardDomainDto> standardDomainDtos = standardDomains.stream()
                        .map(StandardDomainDto::fromEntity)
                        .toList();

                result.put("standardDomains", standardDomainDtos);
            }

        }

        result.put("list", list);

        return result;
    }

    @Override
    @Transactional
    public void insertStandardTerms(AccountDto accountDto, StandardTermDto standardTermDto) {
        String domainName = standardTermDto.getCommonStandardTermAbbreviation();

        // 관리자의 경우 승인으로 저장, 아닐경우 관리자가 승인할 수 있도록 저장
        boolean isApprovalAvailable = AuthUtil.isApprovalAvailable();

        standardTermService.saveStandardTerms(accountDto.getProjectId(), isApprovalAvailable, standardTermDto, domainName.split("_"));
    }

    @Override
    public void updateStandardTerms(AccountDto accountDto, StandardTermDto standardTermDto) {
        String domainName = standardTermDto.getCommonStandardTermAbbreviation();

        // 관리자의 경우 승인으로 저장, 아닐경우 관리자가 승인할 수 있도록 저장
        boolean isApprovalAvailable = AuthUtil.isApprovalAvailable();

        standardTermService.updateStandardTerms(accountDto.getProjectId(), isApprovalAvailable, standardTermDto, domainName.split("_"));
    }

    @Override
    public void insertStandardDomain(AccountDto accountDto, StandardDomainDto standardDomainDto) {

        // 관리자의 경우 승인으로 저장, 아닐경우 관리자가 승인할 수 있도록 저장
        boolean isApprovalAvailable = AuthUtil.isApprovalAvailable();

        standardDomainDto.setProjectId(accountDto.getProjectId());
        standardDomainDto.setIsApprovalYn(isApprovalAvailable ? "Y" : "N");
        StandardDomain standardDomain = StandardDomain.fromEntity(standardDomainDto);

        standardDomainService.insertStandardDomain(standardDomain);
    }

    @Override
    public void updateStandardDomain(AccountDto accountDto, StandardDomainDto standardDomainDto) {
        standardDomainDto.setProjectId(accountDto.getProjectId());

        standardDomainService.updateStandardDomain(standardDomainDto);
    }

    @Override
    public void updateStandardWords(AccountDto accountDto, StandardWordDto standardWordDto) {
        standardWordDto.setProjectId(accountDto.getProjectId());

        standardWordService.updateStandardWords(standardWordDto);
    }

    @Override
    public void insertStandardWord(AccountDto accountDto, StandardWordDto standardWordDto) {
        // 관리자의 경우 승인으로 저장, 아닐경우 관리자가 승인할 수 있도록 저장
        boolean isApprovalAvailable = AuthUtil.isApprovalAvailable();

        standardWordDto.setUseAbbreviation(isApprovalAvailable ? "Y" : "N");
        standardWordDto.setProjectId(accountDto.getProjectId());
        standardWordDto.setIsApprovalYn(isApprovalAvailable ? "Y" : "N");
        StandardWord standardWord = StandardWord.fromEntity(standardWordDto);

        standardWordService.insertStandardWords(standardWord);
    }
}
