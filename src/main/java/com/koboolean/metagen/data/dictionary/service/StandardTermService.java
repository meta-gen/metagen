package com.koboolean.metagen.data.dictionary.service;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTermWordMapping;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
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
import java.util.*;

@Service
@RequiredArgsConstructor
public class StandardTermService {

    private final StandardTermRepository standardTermRepository;
    private static final int FIRST_ROW = 1;
    private final StandardWordService standardWordService;

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

    @Transactional
    public void approvalStandardTerms(List<StandardTermDto> standardTerms, AccountDto accountDto, boolean isApprovalAvailable) {
        Long projectId = accountDto.getProjectId();

        standardTerms.forEach(standardTerm -> {
            StandardTerm term = standardTermRepository.findByIdAndProjectId(standardTerm.getId(), projectId);
            if(term != null){
                // 승인/미승인 상태로 변경한다.
                term.setIsApproval(isApprovalAvailable);
            }
        });
    }

    @Transactional
    public void deleteDataDictionaryStandardTerms(List<StandardTermDto> standardTerms, AccountDto accountDto) {
        standardTerms.forEach(standardDomain -> {
            StandardTerm byIdAndProjectId = standardTermRepository.findByIdAndProjectId(standardDomain.getId(), accountDto.getProjectId());
            if(byIdAndProjectId != null && !byIdAndProjectId.getIsApproval()){
                standardTermRepository.deleteById(standardDomain.getId());
            }
        });
    }

    public void setStandardTerm(MultipartFile file, Long projectId, boolean isApprovalAvailable) throws IOException {
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

            String[] split = standardTermEntry.get("standardTermAbbreviation").split("_");

            saveTermWordMappings(projectId, isApprovalAvailable, standardTermEntry, split);
        });
    }

    /**
     * 표준용어 등록 시 표준 단어와의 Mapping을 수행한다.
     * @param projectId
     * @param isApprovalAvailable
     * @param standardTermEntry
     * @param split
     */
    private void saveTermWordMappings(Long projectId, boolean isApprovalAvailable, Map<String, String> standardTermEntry, String[] split) {
        List<StandardTermWordMapping> mappings = new ArrayList<>();

        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            StandardWord word = standardWordService.findByCommonStandardWordAbbreviation(s, projectId);
            if (word != null) {
                // 일단 매핑 객체는 나중에 StandardTerm set
                StandardTermWordMapping mapping = new StandardTermWordMapping();
                mapping.setStandardWord(word);
                mapping.setOrderIndex(i); // 순서 저장

                mappings.add(mapping);
            }else{
                throw new CustomException(ErrorCode.NOT_FOUND_WORD_DATA, "누락된 문자열 : " + s);
            }
        }

        StandardTerm standardTerm = StandardTerm.builder()
                .revisionNumber(standardTermEntry.get("revisionNumber") == null ? 0 : Integer.parseInt(standardTermEntry.get("revisionNumber").replaceAll("[^0-9]", "")))
                .commonStandardTermName(standardTermEntry.get("standardTermName"))
                .commonStandardTermDescription(standardTermEntry.get("standardTermDescription"))
                .termWordMappings(mappings) // 매핑 넣기
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

        for (StandardTermWordMapping mapping : mappings) {
            mapping.setStandardTerm(standardTerm);
        }

        standardTermRepository.save(standardTerm);
    }
}
