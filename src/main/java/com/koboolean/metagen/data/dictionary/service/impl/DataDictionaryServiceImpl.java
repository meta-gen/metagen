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
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.utils.ExcelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DataDictionaryServiceImpl implements DataDictionaryService {

    private final StandardWordRepository standardWordRepository;
    private final StandardDomainRepository standardDomainRepository;
    private final StandardTermRepository standardTermRepository;

    private static final int FIRST_ROW = 1;

    @Override
    public List<ColumnDto> getStandardTermsColumn() {
        return List.of(
                new ColumnDto("id", "id"),
                new ColumnDto("제정차수", "revisionNumber"),
                new ColumnDto("표준용어명", "commonStandardTermName"),
                new ColumnDto("표준용어설명", "commonStandardTermDescription"),
                new ColumnDto("표준용어영문약어명", "commonStandardTermAbbreviation"),
                new ColumnDto("표준도메인명", "commonStandardDomainName"),
                new ColumnDto("허용값", "allowedValues"),
                new ColumnDto("저장 형식", "storageFormat"),
                new ColumnDto("표현 형식", "displayFormat"),
                new ColumnDto("행정표준코드명", "administrativeStandardCodeName"),
                new ColumnDto("소관기관명", "responsibleOrganization"),
                new ColumnDto("용어 이음동의어 목록", "synonyms")
        );
    }

    @Override
    public List<ColumnDto> getStandardDomainsColumn() {
        return List.of(
                new ColumnDto("id", "id"),
                new ColumnDto("제정차수", "revisionNumber"),
                new ColumnDto("표준도메인그룹명", "commonStandardDomainGroupName"),
                new ColumnDto("표준도메인분류명", "commonStandardDomainCategory"),
                new ColumnDto("표준도메인명", "commonStandardDomainName"),
                new ColumnDto("표준도메인설명", "commonStandardDomainDescription"),
                new ColumnDto("데이터타입", "dataType"),
                new ColumnDto("데이터길이", "dataLength"),
                new ColumnDto("데이터소수점길이", "dataDecimalLength"),
                new ColumnDto("저장 형식", "storageFormat"),
                new ColumnDto("표현 형식", "displayFormat"),
                new ColumnDto("단위", "unit"),
                new ColumnDto("허용값", "allowedValues")
        );
    }

    @Override
    public List<ColumnDto> getStandardWordsColumn() {
        return List.of(
                new ColumnDto("id", "id"),
                new ColumnDto("제정차수", "revisionNumber"),
                new ColumnDto("표준단어명", "commonStandardWordName"),
                new ColumnDto("표준단어영문약어명", "commonStandardWordAbbreviation"),
                new ColumnDto("표준단어 영문명", "commonStandardWordEnglishName"),
                new ColumnDto("표준단어 설명", "commonStandardWordDescription"),
                new ColumnDto("형식단어 여부", "isFormatWord"),
                new ColumnDto("표준도메인분류명", "commonStandardDomainCategory"),
                new ColumnDto("이음동의어 목록", "synonyms"),
                new ColumnDto("금칙어 목록", "restrictedWords")
        );
    }

    @Override
    public Page<StandardTermDto> getStandardTermsData(Pageable pageable, AccountDto accountDto) {
        Page<StandardTerm> allByProjectId = standardTermRepository.findAllByProjectId(accountDto.getProjectId(), pageable);
        return allByProjectId.map(StandardTermDto::fromEntity);
    }

    @Override
    public Page<StandardWordDto> getStandardWordsData(Pageable pageable, AccountDto accountDto) {
        Page<StandardWord> allByProjectId = standardWordRepository.findAllByProjectId(accountDto.getProjectId(), pageable);
        return allByProjectId.map(StandardWordDto::fromEntity);
    }

    @Override
    public Page<StandardDomainDto> getStandardDomainsData(Pageable pageable, AccountDto accountDto) {
        Page<StandardDomain> allByProjectId = standardDomainRepository.findAllByProjectId(accountDto.getProjectId(), pageable);
        return allByProjectId.map(StandardDomainDto::fromEntity);
    }

    @Override
    public void uploadDictionaryExcelFile(MultipartFile file, AccountDto accountDto) throws IOException {

        Long projectId = accountDto.getProjectId();

        setStandardTerm(file, projectId);
        setStandardWord(file, projectId);
        setStandardDomain(file, projectId);
    }

    @Transactional
    protected void setStandardDomain(MultipartFile file, Long projectId) throws IOException {
        List<String> standardDomainHeaders = List.of(
                "id",
                "revisionNumber",
                "standardDomainGroupName",
                "standardDomainCategoryName",
                "standardDomainName",
                "standardDomainDescription",
                "dataType",
                "dataLength",
                "dataDecimalLength",
                "storageFormat",
                "displayFormat",
                "unit",
                "allowedValues"
        );

        List<Map<String, String>> standardDomainData = ExcelUtils.parseExcelFile(file, 2, standardDomainHeaders, false, FIRST_ROW);

        standardDomainData.forEach(standardDomainEntry -> {
            StandardDomain standardDomain = StandardDomain.builder()
                    .revisionNumber(standardDomainEntry.get("revisionNumber") == null ? 0 : Integer.parseInt(standardDomainEntry.get("revisionNumber").replaceAll("[^0-9]", "")))
                    .commonStandardDomainGroupName(standardDomainEntry.get("standardDomainGroupName"))
                    .commonStandardDomainCategory(standardDomainEntry.get("standardDomainCategoryName"))
                    .commonStandardDomainName(standardDomainEntry.get("standardDomainName"))
                    .commonStandardDomainDescription(standardDomainEntry.get("standardDomainDescription"))
                    .dataType(standardDomainEntry.get("dataType"))
                    .dataLength(standardDomainEntry.get("dataLength").equals("-") ? 0 : Integer.parseInt(standardDomainEntry.get("dataLength")))
                    .dataDecimalLength(standardDomainEntry.get("dataDecimalLength").equals("-") ? 0 : Integer.parseInt(standardDomainEntry.get("dataDecimalLength")))
                    .storageFormat(standardDomainEntry.get("storageFormat"))
                    .displayFormat(standardDomainEntry.get("displayFormat"))
                    .unit(standardDomainEntry.get("unit"))
                    .allowedValues(standardDomainEntry.get("allowedValues"))
                    .projectId(projectId)
                    .build();

            standardDomainRepository.save(standardDomain);
        });
    }

    @Transactional
    protected void setStandardWord(MultipartFile file, Long projectId) throws IOException {
        List<String> standardWordHeaders = List.of(
                "id",
                "revisionNumber",
                "standardWordName",
                "standardWordAbbreviation",
                "standardWordEnglishName",
                "standardWordDescription",
                "isFormatWord",
                "standardDomainCategoryName",
                "synonyms",
                "prohibitedWords"
        );
        List<Map<String, String>> standardWordData = ExcelUtils.parseExcelFile(file, 1, standardWordHeaders, false, FIRST_ROW);

        standardWordData.forEach(standardWordEntry -> {
            StandardWord standardWord = StandardWord.builder()
                    .revisionNumber(standardWordEntry.get("revisionNumber") == null ? 0 : Integer.parseInt(standardWordEntry.get("revisionNumber").replaceAll("[^0-9]", "")))
                    .commonStandardWordName(standardWordEntry.get("standardWordName"))
                    .commonStandardWordAbbreviation(standardWordEntry.get("standardWordAbbreviation"))
                    .commonStandardWordEnglishName(standardWordEntry.get("standardWordEnglishName"))
                    .commonStandardWordDescription(standardWordEntry.get("standardWordDescription"))
                    .isFormatWord(standardWordEntry.containsKey("isFormatWord"))
                    .commonStandardDomainCategory(standardWordEntry.get("standardDomainCategoryName"))
                    .synonymList(List.of(standardWordEntry.get("synonyms").split(",")))
                    .restrictedWords(List.of(standardWordEntry.get("prohibitedWords").split(",")))
                    .projectId(projectId)
                    .build();

            standardWordRepository.save(standardWord);
        });
    }

    @Transactional
    protected void setStandardTerm(MultipartFile file, Long projectId) throws IOException {
        List<String> standardTermHeaders = List.of(
                "id",
                "revisionNumber",
                "standardTermName",
                "standardTermDescription",
                "standardTermAbbreviation",
                "standardDomainName",
                "allowedValues",
                "storageFormat",
                "representationFormat",
                "administrativeStandardCodeName",
                "responsibleOrganization",
                "synonyms"
        );
        List<Map<String, String>> standardTermData = ExcelUtils.parseExcelFile(file, 0, standardTermHeaders, false, FIRST_ROW);

        standardTermData.forEach(standardTermEntry -> {
           StandardTerm standardTerm = StandardTerm.builder()
                   .revisionNumber(standardTermEntry.get("revisionNumber") == null ? 0 : Integer.parseInt(standardTermEntry.get("revisionNumber").replaceAll("[^0-9]", "")))
                   .commonStandardTermName(standardTermEntry.get("standardTermName"))
                   .commonStandardTermDescription(standardTermEntry.get("standardTermDescription"))
                   .commonStandardTermAbbreviation(standardTermEntry.get("standardTermAbbreviation"))
                   .commonStandardDomainName(standardTermEntry.get("standardDomainName"))
                   .allowedValues(standardTermEntry.get("allowedValues"))
                   .storageFormat(standardTermEntry.get("storageFormat"))
                   .displayFormat(standardTermEntry.get("representationFormat"))
                   .administrativeStandardCodeName(standardTermEntry.get("administrativeStandardCodeName"))
                   .responsibleOrganization(standardTermEntry.get("responsibleOrganization"))
                   .synonymList(List.of(standardTermEntry.get("synonyms").split(",")))
                   .projectId(projectId)
                   .build();

           standardTermRepository.save(standardTerm);
        });
    }
}
