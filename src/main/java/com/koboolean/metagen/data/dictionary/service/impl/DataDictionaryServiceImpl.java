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
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.utils.AuthUtil;
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
                new ColumnDto("id", "id", ColumnType.NUMBER),
                new ColumnDto("제정차수", "revisionNumber", ColumnType.NUMBER),
                new ColumnDto("표준용어명", "commonStandardTermName", ColumnType.STRING),
                new ColumnDto("표준용어설명", "commonStandardTermDescription", ColumnType.STRING),
                new ColumnDto("표준용어영문약어명", "commonStandardTermAbbreviation", ColumnType.STRING),
                new ColumnDto("표준도메인명", "commonStandardDomainName", ColumnType.STRING),
                new ColumnDto("허용값", "allowedValues", ColumnType.STRING),
                new ColumnDto("저장 형식", "storageFormat", ColumnType.STRING),
                new ColumnDto("표현 형식", "displayFormat", ColumnType.STRING),
                new ColumnDto("행정표준코드명", "administrativeStandardCodeName", ColumnType.STRING),
                new ColumnDto("소관기관명", "responsibleOrganization", ColumnType.STRING),
                new ColumnDto("용어 이음동의어 목록", "synonyms", ColumnType.STRING),
                new ColumnDto("승인여부", "isApprovalYn", ColumnType.STRING)
        );
    }

    @Override
    public List<ColumnDto> getStandardDomainsColumn() {
        return List.of(
                new ColumnDto("id", "id", ColumnType.NUMBER),
                new ColumnDto("제정차수", "revisionNumber", ColumnType.NUMBER),
                new ColumnDto("데이터길이", "dataLength", ColumnType.NUMBER),
                new ColumnDto("데이터소수점길이", "dataDecimalLength", ColumnType.NUMBER),
                new ColumnDto("표준도메인그룹명", "commonStandardDomainGroupName", ColumnType.STRING),
                new ColumnDto("표준도메인분류명", "commonStandardDomainCategory", ColumnType.STRING),
                new ColumnDto("표준도메인명", "commonStandardDomainName", ColumnType.STRING),
                new ColumnDto("표준도메인설명", "commonStandardDomainDescription", ColumnType.STRING),
                new ColumnDto("데이터타입", "dataType", ColumnType.STRING),
                new ColumnDto("저장 형식", "storageFormat", ColumnType.STRING),
                new ColumnDto("표현 형식", "displayFormat", ColumnType.STRING),
                new ColumnDto("단위", "unit", ColumnType.STRING),
                new ColumnDto("허용값", "allowedValues", ColumnType.STRING),
                new ColumnDto("승인여부", "isApprovalYn", ColumnType.STRING)
        );
    }

    @Override
    public List<ColumnDto> getStandardWordsColumn() {
        return List.of(
                new ColumnDto("id", "id", ColumnType.NUMBER),
                new ColumnDto("제정차수", "revisionNumber", ColumnType.NUMBER),
                new ColumnDto("표준단어명", "commonStandardWordName", ColumnType.STRING),
                new ColumnDto("표준단어영문약어명", "commonStandardWordAbbreviation", ColumnType.STRING),
                new ColumnDto("표준단어 영문명", "commonStandardWordEnglishName", ColumnType.STRING),
                new ColumnDto("표준단어 설명", "commonStandardWordDescription", ColumnType.STRING),
                new ColumnDto("표준도메인분류명", "commonStandardDomainCategory", ColumnType.STRING),
                new ColumnDto("형식단어 여부", "isFormatWord", ColumnType.STRING),
                new ColumnDto("이음동의어 목록", "synonyms", ColumnType.STRING),
                new ColumnDto("금칙어 목록", "restrictedWords", ColumnType.STRING),
                new ColumnDto("승인여부", "isApprovalYn", ColumnType.STRING)
        );
    }

    @Override
    public Page<StandardTermDto> getStandardTermsData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // 검색어가 없을 경우 전체 조회
            Page<StandardTerm> allByProjectId = standardTermRepository.findAllByProjectId(accountDto.getProjectId(), pageable);
            return allByProjectId.map(StandardTermDto::fromEntity);
        }

        Page<StandardTerm> searchResult = switch (searchColumn) {
            case "id" -> standardTermRepository.findAllByIdAndProjectId(Long.parseLong(searchQuery.replaceAll("[^0-9]", "")), Long.parseLong(accountDto.getProjectId().toString()), pageable);
            case "revisionNumber" ->
                    standardTermRepository.findByRevisionNumberAndProjectId(Integer.parseInt(searchQuery.replaceAll("[^0-9]", "")), accountDto.getProjectId(), pageable);
            case "commonStandardTermName" ->
                    standardTermRepository.findByCommonStandardTermNameContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "commonStandardTermDescription" ->
                    standardTermRepository.findByCommonStandardTermDescriptionContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "commonStandardTermAbbreviation" ->
                    standardTermRepository.findByCommonStandardTermAbbreviationContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "commonStandardDomainName" ->
                    standardTermRepository.findByCommonStandardDomainNameContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "allowedValues" ->
                    standardTermRepository.findByAllowedValuesContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "storageFormat" ->
                    standardTermRepository.findByStorageFormatContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "displayFormat" ->
                    standardTermRepository.findByDisplayFormatContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "administrativeStandardCodeName" ->
                    standardTermRepository.findByAdministrativeStandardCodeNameContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "responsibleOrganization" ->
                    standardTermRepository.findByResponsibleOrganizationContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "synonyms" ->
                    standardTermRepository.findBySynonymListContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "isApproval" ->
                    standardTermRepository.findByIsApprovalAndProjectId(searchQuery.equals("Y"), accountDto.getProjectId(), pageable);
            default ->
                    standardTermRepository.findByCommonStandardTermNameContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
        };

        return searchResult.map(StandardTermDto::fromEntity);
    }

    @Override
    public Page<StandardWordDto> getStandardWordsData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // 검색어가 없을 경우 전체 조회
            Page<StandardWord> allByProjectId = standardWordRepository.findAllByProjectId(accountDto.getProjectId(), pageable);
            return allByProjectId.map(StandardWordDto::fromEntity);
        }

        Page<StandardWord> searchResult = switch (searchColumn) {
            case "id" -> standardWordRepository.findAllByIdAndProjectId(Long.parseLong(searchQuery.replaceAll("[^0-9]", "")), Long.parseLong(accountDto.getProjectId().toString()), pageable);
            case "revisionNumber" ->
                    standardWordRepository.findByRevisionNumberContainingAndProjectId(Integer.parseInt(searchQuery.replaceAll("[^0-9]", "")), accountDto.getProjectId(), pageable);
            case "commonStandardWordName" ->
                    standardWordRepository.findByCommonStandardWordNameContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "commonStandardWordAbbreviation" ->
                    standardWordRepository.findByCommonStandardWordAbbreviationContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "commonStandardWordEnglishName" ->
                    standardWordRepository.findByCommonStandardWordEnglishNameContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "commonStandardWordDescription" ->
                    standardWordRepository.findByCommonStandardWordDescriptionContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "isFormatWord" ->
                    standardWordRepository.findByIsFormatWordAndProjectId(searchQuery.equals("Y"), accountDto.getProjectId(), pageable);
            case "commonStandardDomainCategory" ->
                    standardWordRepository.findByCommonStandardDomainCategoryContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "synonyms" ->
                    standardWordRepository.findBySynonymListContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "restrictedWords" ->
                    standardWordRepository.findByRestrictedWordsContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "isApproval" ->
                    standardWordRepository.findByIsApprovalAndProjectId(searchQuery.equals("Y"), accountDto.getProjectId(), pageable);
            default ->
                    standardWordRepository.findByCommonStandardWordNameContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
        };

        return searchResult.map(StandardWordDto::fromEntity);
    }

    @Override
    public Page<StandardDomainDto> getStandardDomainsData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            Page<StandardDomain> allByProjectId = standardDomainRepository.findAllByProjectId(accountDto.getProjectId(), pageable);
            return allByProjectId.map(StandardDomainDto::fromEntity);
        }

        Page<StandardDomain> searchResult = switch (searchColumn) {
            case "id" -> standardDomainRepository.findAllByIdAndProjectId(Long.parseLong(searchQuery.replaceAll("[^0-9]", "")), Long.parseLong(accountDto.getProjectId().toString()), pageable);
            case "revisionNumber" ->
                    standardDomainRepository.findByRevisionNumberContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "commonStandardDomainGroupName" ->
                    standardDomainRepository.findByCommonStandardDomainGroupNameContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "commonStandardDomainCategory" ->
                    standardDomainRepository.findByCommonStandardDomainCategoryContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "commonStandardDomainName" ->
                    standardDomainRepository.findByCommonStandardDomainNameContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "commonStandardDomainDescription" ->
                    standardDomainRepository.findByCommonStandardDomainDescriptionContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "dataType" ->
                    standardDomainRepository.findByDataTypeContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "dataLength" ->
                    standardDomainRepository.findByDataLengthContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "dataDecimalLength" ->
                    standardDomainRepository.findByDataDecimalLengthContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "storageFormat" ->
                    standardDomainRepository.findByStorageFormatContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "displayFormat" ->
                    standardDomainRepository.findByDisplayFormatContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "unit" ->
                    standardDomainRepository.findByUnitContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "allowedValues" ->
                    standardDomainRepository.findByAllowedValuesContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "isApproval" ->
                    standardDomainRepository.findByIsApprovalAndProjectId(searchQuery.equals("Y"), accountDto.getProjectId(), pageable);
            default ->
                    standardDomainRepository.findByCommonStandardDomainNameContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
        };

        return searchResult.map(StandardDomainDto::fromEntity);
    }

    @Override
    public void uploadDictionaryExcelFile(MultipartFile file, AccountDto accountDto) throws IOException {

        Long projectId = accountDto.getProjectId();

        // 관리자의 경우 승인으로 저장, 아닐경우 관리자가 승인할 수 있도록 저장
        boolean isApprovalAvailable = AuthUtil.isIsApprovalAvailable();

        setStandardTerm(file, projectId, isApprovalAvailable);
        setStandardWord(file, projectId, isApprovalAvailable);
        setStandardDomain(file, projectId, isApprovalAvailable);
    }

    @Transactional
    protected void setStandardDomain(MultipartFile file, Long projectId, boolean isApprovalAvailable) throws IOException {
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
                    .isApproval(isApprovalAvailable)
                    .build();

            standardDomainRepository.save(standardDomain);
        });
    }

    @Transactional
    protected void setStandardWord(MultipartFile file, Long projectId, boolean isApprovalAvailable) throws IOException {
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
                    .isApproval(isApprovalAvailable)
                    .build();

            standardWordRepository.save(standardWord);
        });
    }

    @Transactional
    protected void setStandardTerm(MultipartFile file, Long projectId, boolean isApprovalAvailable) throws IOException {
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
                   .isApproval(isApprovalAvailable)
                   .build();

           standardTermRepository.save(standardTerm);
        });
    }
}
