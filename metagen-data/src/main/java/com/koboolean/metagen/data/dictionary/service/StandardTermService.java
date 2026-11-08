package com.koboolean.metagen.data.dictionary.service;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTermWordMapping;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardDomainRepository;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
import com.koboolean.metagen.data.dictionary.repository.StandardWordRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.common.utils.ExcelUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StandardTermService {

    private final StandardTermRepository standardTermRepository;

    private final StandardDomainService standardDomainService;
    private final StandardWordService standardWordService;
    private final StandardWordRepository standardWordRepository;
    private final StandardDomainRepository standardDomainRepository;
    private final EntityManager entityManager;

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
        // 업로드 중에는 단어/도메인이 바뀌지 않으므로 조회 결과를 캐시한다. (영문약어 → 단어 목록, 도메인명 → 도메인 ID)
        Map<String, List<StandardWord>> wordsByAbbreviation = new HashMap<>();
        Map<String, Long> domainIdByName = new HashMap<>();

        // 청크(500행) 단위 : 단어/도메인은 청크당 IN 조회, 저장은 JDBC 배치(용어 + 매핑 + 이음동의어), 저장 후 영속성 컨텍스트 비우기
        ExcelUtils.readExcelRowsInChunks(file, 0, standardTermHeaders, FIRST_ROW, ExcelUtils.UPLOAD_CHUNK_SIZE, rows -> {
            List<Map<String, String>> termRows = rows.stream()
                    .filter(row -> row.get("standardTermName") != null && !row.get("standardTermName").isEmpty())
                    .toList();

            loadUploadReferences(projectId, termRows, wordsByAbbreviation, domainIdByName);

            List<StandardTerm> standardTerms = new ArrayList<>();
            for (Map<String, String> standardTermEntry : termRows) {
                standardTerms.add(buildUploadedTerm(projectId, isApprovalAvailable, standardTermEntry, wordsByAbbreviation, domainIdByName));
            }

            standardTermRepository.saveAll(standardTerms);
            entityManager.flush();
            entityManager.clear(); // 캐시한 단어는 detach 되지만 ID 로 참조(getReference)하므로 문제없다
        });
    }

    /**
     * 청크의 용어가 쓰는 단어(영문약어)/도메인 중 아직 캐시에 없는 것을 IN 조회 한 번씩으로 불러온다.
     */
    private void loadUploadReferences(Long projectId, List<Map<String, String>> termRows,
                                      Map<String, List<StandardWord>> wordsByAbbreviation, Map<String, Long> domainIdByName) {
        Set<String> abbreviations = termRows.stream()
                .flatMap(row -> Arrays.stream(row.get("standardTermAbbreviation").split("_")))
                .filter(abbreviation -> !wordsByAbbreviation.containsKey(abbreviation))
                .collect(Collectors.toSet());

        if (!abbreviations.isEmpty()) {
            abbreviations.forEach(abbreviation -> wordsByAbbreviation.put(abbreviation, new ArrayList<>()));
            standardWordRepository.findAllByProjectIdAndCommonStandardWordAbbreviationIn(projectId, abbreviations)
                    .forEach(word -> wordsByAbbreviation.get(word.getCommonStandardWordAbbreviation()).add(word));
        }

        Set<String> domainNames = termRows.stream()
                .map(row -> row.get("standardDomainName"))
                .filter(name -> name != null && !domainIdByName.containsKey(name))
                .collect(Collectors.toSet());

        if (!domainNames.isEmpty()) {
            standardDomainRepository.findAllByProjectIdAndCommonStandardDomainNameIn(projectId, domainNames)
                    .forEach(domain -> domainIdByName.putIfAbsent(domain.getCommonStandardDomainName(), domain.getId()));
        }
    }

    /**
     * 엑셀 한 행으로 표준용어를 만든다. (단어 매핑 / 도메인은 조회 없이 ID 참조로 연결)
     * 검증 순서와 오류는 기존과 같다 : 단어 매핑(NOT_FOUND_WORD_DATA) → 도메인(NOT_FOUND_DOMAIN_DATA)
     */
    private StandardTerm buildUploadedTerm(Long projectId, boolean isApprovalAvailable, Map<String, String> standardTermEntry,
                                           Map<String, List<StandardWord>> wordsByAbbreviation, Map<String, Long> domainIdByName) {
        String standardTermName = standardTermEntry.get("standardTermName");
        String abbreviation = standardTermEntry.get("standardTermAbbreviation");

        // 용어명과 영문약어명의 단어 수가 다르면 업로드 전체를 취소한다. (사용자_이름 / USER_NM)
        validateWordCount(standardTermName, abbreviation);

        String[] split = abbreviation.split("_");
        String[] nameWords = standardTermName.split("_");

        List<StandardTermWordMapping> mappings = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            // 같은 순번의 단어명과 정확히 같은 단어를 쓴다.
            StandardWord word = findWord(wordsByAbbreviation.getOrDefault(split[i], List.of()), nameWords[i]);

            if (word == null) {
                throw new CustomException(ErrorCode.NOT_FOUND_WORD_DATA, "누락된 문자열 : " + split[i]);
            }

            StandardTermWordMapping mapping = new StandardTermWordMapping();
            mapping.setStandardWord(entityManager.getReference(StandardWord.class, word.getId()));
            mapping.setOrderIndex(i);
            mappings.add(mapping);
        }

        Long domainId = domainIdByName.get(standardTermEntry.get("standardDomainName"));
        if (domainId == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_DOMAIN_DATA);
        }

        StandardTerm standardTerm = StandardTerm.builder()
                .revisionNumber(standardTermEntry.get("revisionNumber") == null || standardTermEntry.get("revisionNumber").isEmpty() ? 0 : Integer.parseInt(standardTermEntry.get("revisionNumber").replaceAll("[^0-9]", "")))
                .commonStandardTermName(standardTermName)
                .commonStandardTermDescription(standardTermEntry.get("standardTermDescription"))
                .termWordMappings(mappings)
                .commonStandardTermAbbreviation(standardTermEntry.get("standardTermAbbreviation"))
                .administrativeStandardCodeName(standardTermEntry.get("administrativeStandardCodeName"))
                .responsibleOrganization(standardTermEntry.get("responsibleOrganization"))
                .synonymList(List.of(standardTermEntry.get("synonyms").split(",")))
                .projectId(projectId)
                .isApproval(isApprovalAvailable)
                .standardDomain(entityManager.getReference(StandardDomain.class, domainId))
                .build();

        mappings.forEach(mapping -> mapping.setStandardTerm(standardTerm));
        return standardTerm;
    }

    private void getStandardTermDomain(Long projectId, String[] split, List<StandardTermWordMapping> mappings, String standardTermName) {
        String[] nameWords = standardTermName.split("_", -1);

        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            List<StandardWord> words = standardWordService.findByCommonStandardWordAbbreviation(s, projectId);

            // 같은 순번의 단어명과 정확히 같은 단어를 쓴다. (사용자_이름 / USER_NM → 사용자=USER, 이름=NM)
            StandardWord word = findWord(words, nameWords[i]);

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

    /**
     * 표준용어명과 영문약어명은 '_' 로 나눈 단어 수가 같아야 한다. (빈 단어 / 공백 구분 불가)
     * 예) 사용자_이름 / USER_NM  (O),  사용자이름 / USER_NM  (X),  사용자 이름 / USER_NM  (X)
     */
    public static void validateWordCount(String termName, String abbreviation) {
        String name = termName == null ? "" : termName.trim();
        String abbr = abbreviation == null ? "" : abbreviation.trim();

        String[] nameWords = name.split("_", -1);
        String[] abbrWords = abbr.split("_", -1);

        boolean hasBlank = Arrays.stream(nameWords).anyMatch(String::isBlank) || Arrays.stream(abbrWords).anyMatch(String::isBlank)
                || name.chars().anyMatch(Character::isWhitespace);

        if (name.isEmpty() || abbr.isEmpty() || hasBlank || nameWords.length != abbrWords.length) {
            throw new CustomException(ErrorCode.TERM_WORD_COUNT_MISMATCH, termName + " / " + abbreviation);
        }
    }

    /**
     * 같은 영문약어의 단어 중 이름이 정확히 같은 단어 (없으면 null)
     */
    private static StandardWord findWord(List<StandardWord> candidates, String wordName) {
        return candidates.stream()
                .filter(candidate -> Objects.equals(candidate.getCommonStandardWordName(), wordName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 같은 영문약어명 + 도메인으로 이미 등록된 용어 (등록 시 중복 기준)
     */
    public List<StandardTerm> findDuplicateTerms(String abbreviation, String domainName, Long projectId) {
        return standardTermRepository.findByCommonStandardTermAbbreviationAndStandardDomain_CommonStandardDomainNameAndProjectId(abbreviation, domainName, projectId);
    }

    public void saveStandardTerms(Long projectId, boolean isApprovalAvailable, StandardTermDto standardTermDto, String[] split) {

        validateWordCount(standardTermDto.getCommonStandardTermName(), standardTermDto.getCommonStandardTermAbbreviation());

        List<StandardTerm> byCommonStandardTermAbbreviationAndProjectId = findDuplicateTerms(standardTermDto.getCommonStandardTermAbbreviation(), standardTermDto.getCommonStandardDomainName(), projectId);

        if(!byCommonStandardTermAbbreviationAndProjectId.isEmpty()){
            // 이미 등록된 정보가 있기 떄문에 Error처리
            throw new CustomException(ErrorCode.DUPLICATE_TERM_DATA);
        }

        List<StandardTermWordMapping> mappings = new ArrayList<>();

        getStandardTermDomain(projectId, split, mappings,standardTermDto.getCommonStandardTermName());

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

        if (!Objects.equals(existingTerm.getCommonStandardTermName(), dto.getCommonStandardTermName())
                || !Objects.equals(existingTerm.getCommonStandardTermAbbreviation(), dto.getCommonStandardTermAbbreviation())) {
            validateWordCount(dto.getCommonStandardTermName(), dto.getCommonStandardTermAbbreviation());
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
