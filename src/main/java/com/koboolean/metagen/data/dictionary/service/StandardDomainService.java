package com.koboolean.metagen.data.dictionary.service;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.repository.StandardDomainRepository;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
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
        standardDomains.forEach(domainDto -> {
            StandardDomain domain = standardDomainRepository.findByIdAndProjectId(domainDto.getId(), accountDto.getProjectId());

            if (domain != null) {
                // 1. 승인된 도메인 삭제 불가
                if (Boolean.TRUE.equals(domain.getIsApproval())) {
                    throw new CustomException(ErrorCode.APPROVED_DATA_CANNOT_BE_DELETED,
                            "승인된 도메인은 삭제할 수 없습니다: " + domain.getCommonStandardDomainName());
                }

                // 2. 표준용어에서 참조 중인 경우 삭제 불가
                if (domain.getStandardTerms() != null && !domain.getStandardTerms().isEmpty()) {
                    throw new CustomException(ErrorCode.RELATION_EXISTS,
                            "표준용어에서 사용 중인 도메인은 삭제할 수 없습니다: " + domain.getCommonStandardDomainName());
                }

                // 3. 삭제 수행
                standardDomainRepository.delete(domain);
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
                    .revisionNumber(standardDomainEntry.get("revisionNumber") == null || standardDomainEntry.get("revisionNumber").isEmpty() ? 0 : Integer.parseInt(standardDomainEntry.get("revisionNumber").replaceAll("[^0-9]", "")))
                    .commonStandardDomainGroupName(standardDomainEntry.get("standardDomainGroupName"))
                    .commonStandardDomainCategory(standardDomainEntry.get("standardDomainCategoryName"))
                    .commonStandardDomainName(standardDomainEntry.get("standardDomainName"))
                    .commonStandardDomainDescription(standardDomainEntry.get("standardDomainDescription"))
                    .dataType(standardDomainEntry.get("dataType"))
                    .dataLength(standardDomainEntry.get("dataLength").equals("-") || standardDomainEntry.get("dataLength").isEmpty()  ? 0 : Integer.parseInt(standardDomainEntry.get("dataLength")))
                    .dataDecimalLength(standardDomainEntry.get("dataDecimalLength").equals("-")  || standardDomainEntry.get("dataDecimalLength").isEmpty() ? 0 : Integer.parseInt(standardDomainEntry.get("dataDecimalLength")))
                    .storageFormat(standardDomainEntry.get("storageFormat"))
                    .displayFormat(standardDomainEntry.get("displayFormat"))
                    .unit(standardDomainEntry.get("unit"))
                    .allowedValues(standardDomainEntry.get("allowedValues"))
                    .projectId(projectId)
                    .isApproval(isApprovalAvailable)
                    .build();

            if(standardDomain.getCommonStandardDomainName() != null && !standardDomain.getCommonStandardDomainName().isEmpty()){
                standardDomainRepository.save(standardDomain);
            }
        });
    }

    public List<StandardDomain> getStandardDomains(String commonStandardDomainCategory, Long projectId) {
        return standardDomainRepository.findAllByCommonStandardDomainCategoryAndProjectId(commonStandardDomainCategory, projectId);
    }

    public StandardDomain findByCommonStandardDomainNameAndProjectId(String domainName, Long projectId) {
        return standardDomainRepository.findByCommonStandardDomainNameAndProjectId(domainName, projectId);
    }

    @Transactional
    public void insertStandardDomain(StandardDomain standardDomain) {
        standardDomainRepository.save(standardDomain);
    }

    @Transactional
    public void updateStandardDomain(StandardDomainDto standardDomainDto) {
        StandardDomain standardDomain = standardDomainRepository.findByIdAndProjectId(standardDomainDto.getId(), standardDomainDto.getProjectId());

        standardDomain.setRevisionNumber(standardDomainDto.getRevisionNumber());
        standardDomain.setCommonStandardDomainDescription(standardDomainDto.getCommonStandardDomainDescription());
        standardDomain.setDataType(standardDomainDto.getDataType());
        standardDomain.setDataLength(standardDomainDto.getDataLength());
        standardDomain.setDataDecimalLength(standardDomainDto.getDataDecimalLength());
        standardDomain.setUnit(standardDomainDto.getUnit());
    }
}
