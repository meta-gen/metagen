package com.koboolean.metagen.data.dictionary.service;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTermWordMapping;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardDomainRepository;
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

    private final StandardDomainService standardDomainService;
    private final StandardWordService standardWordService;

    private static final int FIRST_ROW = 1;

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
                    standardTermRepository.findByStandardDomain_CommonStandardDomainNameContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "allowedValues" ->
                    standardTermRepository.findByStandardDomain_AllowedValuesContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "storageFormat" ->
                    standardTermRepository.findByStandardDomain_StorageFormatContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
            case "displayFormat" ->
                    standardTermRepository.findByStandardDomain_DisplayFormatContainingAndProjectId(searchQuery, accountDto.getProjectId(), pageable);
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

        getStandardTermDomain(projectId, split, mappings);

        StandardTerm standardTerm = getStandardTerm(projectId, isApprovalAvailable, standardTermEntry, mappings);

        for (StandardTermWordMapping mapping : mappings) {
            mapping.setStandardTerm(standardTerm);
        }

        standardTermRepository.save(standardTerm);
    }

    private StandardTerm getStandardTerm(Long projectId, boolean isApprovalAvailable, Map<String, String> standardTermEntry, List<StandardTermWordMapping> mappings) {
        String domainName = standardTermEntry.get("standardDomainName");

        StandardDomain standardDomain = standardDomainService.findByCommonStandardDomainNameAndProjectId(domainName, projectId);

        if (standardDomain == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_DOMAIN_DATA);
        }

        return StandardTerm.builder()
                .revisionNumber(standardTermEntry.get("revisionNumber") == null ? 0 : Integer.parseInt(standardTermEntry.get("revisionNumber").replaceAll("[^0-9]", "")))
                .commonStandardTermName(standardTermEntry.get("standardTermName"))
                .commonStandardTermDescription(standardTermEntry.get("standardTermDescription"))
                .termWordMappings(mappings)
                .commonStandardTermAbbreviation(standardTermEntry.get("standardTermAbbreviation"))
                .administrativeStandardCodeName(standardTermEntry.get("administrativeStandardCodeName"))
                .responsibleOrganization(standardTermEntry.get("responsibleOrganization"))
                .synonymList(List.of(standardTermEntry.get("synonyms").split(",")))
                .projectId(projectId)
                .isApproval(isApprovalAvailable)
                .standardDomain(standardDomain) // 연관관계 주입
                .build();
    }

    private void getStandardTermDomain(Long projectId, String[] split, List<StandardTermWordMapping> mappings) {
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
    }

    public void saveStandardTerms(Long projectId, boolean isApprovalAvailable, StandardTermDto standardTermDto, String[] split) {

        List<StandardTerm> byCommonStandardTermAbbreviationAndProjectId = standardTermRepository.findByCommonStandardTermAbbreviationAndStandardDomain_CommonStandardDomainNameAndProjectId(standardTermDto.getCommonStandardTermAbbreviation(), standardTermDto.getCommonStandardDomainName(), projectId);

        if(!byCommonStandardTermAbbreviationAndProjectId.isEmpty()){
            // 이미 등록된 정보가 있기 떄문에 Error처리
            throw new CustomException(ErrorCode.DUPLICATE_TERM_DATA);
        }

        List<StandardTermWordMapping> mappings = new ArrayList<>();

        getStandardTermDomain(projectId, split, mappings);

        StandardTerm standardTerm = getStandardTerm(projectId, isApprovalAvailable, standardTermDto, mappings);

        for (StandardTermWordMapping mapping : mappings) {
            mapping.setStandardTerm(standardTerm);
        }

        standardTermRepository.save(standardTerm);
    }

    private StandardTerm getStandardTerm(Long projectId, boolean isApprovalAvailable, StandardTermDto dto, List<StandardTermWordMapping> mappings) {
        StandardDomain standardDomain = standardDomainService.findByCommonStandardDomainNameAndProjectId(
                dto.getCommonStandardDomainName(), projectId
        );

        if (standardDomain == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_DOMAIN_DATA,
                    "도메인을 찾을 수 없습니다: " + dto.getCommonStandardDomainName());
        }

        return StandardTerm.builder()
                .revisionNumber(dto.getRevisionNumber())
                .commonStandardTermName(dto.getCommonStandardTermName())
                .commonStandardTermDescription(dto.getCommonStandardTermDescription())
                .termWordMappings(mappings)
                .commonStandardTermAbbreviation(dto.getCommonStandardTermAbbreviation())
                .administrativeStandardCodeName(dto.getAdministrativeStandardCodeName())
                .responsibleOrganization(dto.getResponsibleOrganization())
                .synonymList(List.of(dto.getSynonyms().split(",")))
                .projectId(projectId)
                .isApproval(isApprovalAvailable)
                .standardDomain(standardDomain) // 연관관계 설정
                .build();
    }

    @Transactional
    public void updateStandardTerms(Long projectId, boolean isApprovalAvailable, StandardTermDto dto, String[] split) {
        StandardTerm existingTerm = standardTermRepository.findByIdAndProjectId(dto.getId(), projectId);
        if (existingTerm == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_TERM_DATA);
        }

        StandardDomain standardDomain = standardDomainService.findByCommonStandardDomainNameAndProjectId(
                dto.getCommonStandardDomainName(), projectId
        );

        if (standardDomain == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_DOMAIN_DATA,
                    "도메인을 찾을 수 없습니다: " + dto.getCommonStandardDomainName());
        }

        existingTerm.setRevisionNumber(dto.getRevisionNumber());
        existingTerm.setCommonStandardTermName(dto.getCommonStandardTermName());
        existingTerm.setCommonStandardTermDescription(dto.getCommonStandardTermDescription());
        existingTerm.setCommonStandardTermAbbreviation(dto.getCommonStandardTermAbbreviation());
        existingTerm.setAdministrativeStandardCodeName(dto.getAdministrativeStandardCodeName());
        existingTerm.setResponsibleOrganization(dto.getResponsibleOrganization());
        existingTerm.setSynonymList(List.of(dto.getSynonyms().split(",")));
        existingTerm.setIsApproval(isApprovalAvailable);
        existingTerm.setStandardDomain(standardDomain); // 연관관계 주입

        // 단어 매핑도 갱신 필요 시 추가
    }
}
