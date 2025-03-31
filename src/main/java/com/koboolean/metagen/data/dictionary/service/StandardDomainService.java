package com.koboolean.metagen.data.dictionary.service;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.repository.StandardDomainRepository;
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
public class StandardDomainService {

    private final StandardDomainRepository standardDomainRepository;
    private static final int FIRST_ROW = 1;

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

    @Transactional
    public void approvalStandardDomains(List<StandardDomainDto> standardDomains, AccountDto accountDto, boolean isApprovalAvailable) {

        Long projectId = accountDto.getProjectId();

        standardDomains.forEach(standardDomain -> {
            StandardDomain domain = standardDomainRepository.findByIdAndProjectId(standardDomain.getId(), projectId);
            if(domain != null){
                // 승인/미승인 상태로 변경한다.
                domain.setIsApproval(isApprovalAvailable);
            }
        });
    }

    @Transactional
    public void deleteDataDictionaryStandardDomains(List<StandardDomainDto> standardDomains, AccountDto accountDto) {
        standardDomains.forEach(standardDomain -> {
            StandardDomain byIdAndProjectId = standardDomainRepository.findByIdAndProjectId(standardDomain.getId(), accountDto.getProjectId());
            if(byIdAndProjectId != null && !byIdAndProjectId.getIsApproval()){
                standardDomainRepository.deleteById(standardDomain.getId());
            }
        });
    }

    public void setStandardDomain(MultipartFile file, Long projectId, boolean isApprovalAvailable) throws IOException {
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

    public List<StandardDomain> getStandardDomains(String splitDatum, Long projectId) {
        return standardDomainRepository.findAllByCommonStandardDomainCategoryAndProjectId(splitDatum, projectId);
    }
}
