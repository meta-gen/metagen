package com.koboolean.metagen.data.dictionary.service;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardDomainRepository;
import com.koboolean.metagen.data.dictionary.repository.StandardWordRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.common.utils.ExcelUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StandardWordService {

    private static final int FIRST_ROW = 1;
    private final StandardWordRepository standardWordRepository;
    private final StandardDomainRepository standardDomainRepository;
    private final EntityManager entityManager;

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

    @Transactional
    public void approvalStandardWords(List<StandardWordDto> standardWords, AccountDto accountDto, boolean isApprovalAvailable) {
        Long projectId = accountDto.getProjectId();

        standardWords.forEach(standardWord -> {
            StandardWord word = standardWordRepository.findByIdAndProjectId(standardWord.getId(), projectId);
            if(word != null){
                // 승인/미승인 상태로 변경한다.
                word.setIsApproval(isApprovalAvailable);
            }
        });
    }

    @Transactional
    public void deleteDataDictionaryStandardWords(List<StandardWordDto> standardWords, AccountDto accountDto) {
        standardWords.forEach(standardDomain -> {
            StandardWord word = standardWordRepository.findByIdAndProjectId(standardDomain.getId(), accountDto.getProjectId());

            if (word != null) {
                if (word.getIsApproval()) {
                    throw new CustomException(ErrorCode.APPROVED_DATA_CANNOT_BE_DELETED, word.getCommonStandardWordName());
                }

                if (word.getTermWordMappings() != null && !word.getTermWordMappings().isEmpty()) {
                    throw new CustomException(ErrorCode.RELATION_EXISTS, word.getCommonStandardWordName());
                }

                standardWordRepository.deleteById(word.getId());
            }
        });
    }

    public void setStandardWord(MultipartFile file, Long projectId, boolean isApprovalAvailable) throws IOException {
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
        // 파일 안 중복 확인용 / 도메인 분류 등록 여부 캐시 (분류 수만큼만 조회)
        Set<String> uploadedWordNames = new HashSet<>();
        Map<String, Boolean> domainCategoryExists = new HashMap<>();

        // 청크(500행) 단위 : 중복 단어명은 청크당 IN 조회 한 번, 저장은 JDBC 배치, 저장 후 영속성 컨텍스트 비우기
        ExcelUtils.readExcelRowsInChunks(file, 1, standardWordHeaders, FIRST_ROW, ExcelUtils.UPLOAD_CHUNK_SIZE, rows -> {
            Set<String> chunkWordNames = rows.stream()
                    .map(row -> row.get("standardWordName"))
                    .filter(name -> name != null && !name.isEmpty())
                    .collect(Collectors.toSet());

            Set<String> existingWordNames = chunkWordNames.isEmpty() ? Set.of()
                    : standardWordRepository.findAllByProjectIdAndCommonStandardWordNameIn(projectId, chunkWordNames).stream()
                        .map(StandardWord::getCommonStandardWordName)
                        .collect(Collectors.toSet());

            List<StandardWord> standardWords = new ArrayList<>();

            for (Map<String, String> standardWordEntry : rows) {
                String wordName = standardWordEntry.get("standardWordName");
                if (wordName == null || wordName.isEmpty()) {
                    continue;
                }

                StandardWord standardWord = StandardWord.builder()
                        .revisionNumber(standardWordEntry.get("revisionNumber") == null || standardWordEntry.get("revisionNumber").isEmpty() ? 0 : Integer.parseInt(standardWordEntry.get("revisionNumber").replaceAll("[^0-9]", "")))
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
                        .useAbbreviation(standardWordEntry.get("standardWordEnglishName").split(" ").length > 1)
                        .build();

                // 이미 등록된 단어이거나 파일 안에서 중복되면 업로드 전체를 취소한다.
                if (existingWordNames.contains(wordName) || !uploadedWordNames.add(wordName)) {
                    throw new CustomException(ErrorCode.SAVED_DATA_EXISTS, wordName);
                }

                String domainCategory = standardWord.getCommonStandardDomainCategory();
                if (domainCategory != null && !domainCategory.isEmpty() && !domainCategory.equals("-")
                        && !domainCategoryExists.computeIfAbsent(domainCategory,
                            category -> standardDomainRepository.existsByProjectIdAndCommonStandardDomainCategory(projectId, category))) {
                    throw new CustomException(ErrorCode.DOMAIN_IS_NOT_DEFINED, domainCategory);
                }

                standardWords.add(standardWord);
            }

            standardWordRepository.saveAll(standardWords);
            entityManager.flush();
            entityManager.clear();
        });
    }

    public List<StandardWord> findByCommonStandardWordAbbreviation(String s, Long projectId) {
        return standardWordRepository.findAllByCommonStandardWordAbbreviationAndProjectId(s, projectId);
    }

    public StandardWord commonStandardWordName(String s, Long projectId) {
        return standardWordRepository.findByCommonStandardWordNameAndProjectId(s, projectId);
    }

    @Transactional
    public void insertStandardWords(StandardWord standardWord) {
        List<StandardWord> nameIsDupl = standardWordRepository.findAllByProjectIdAndCommonStandardWordName(standardWord.getProjectId(), standardWord.getCommonStandardWordName());

        if(nameIsDupl.isEmpty()){
            domainIsNotDefinedCheck(standardWord);

            standardWordRepository.save(standardWord);
        }else{
            throw new CustomException(ErrorCode.SAVED_DATA_EXISTS, standardWord.getCommonStandardWordName());
        }

    }

    /**
     * 도메인이 등록되어 있는경우, 도메인 정보를 조회해와 일치하는 도메인이 존재하지 않으면 Error
     * @param standardWord
     */
    private void domainIsNotDefinedCheck(StandardWord standardWord) {
        String domainCategory = standardWord.getCommonStandardDomainCategory();

        if(!domainCategory.isEmpty() && !domainCategory.equals("-")){
            List<StandardDomain> standardDomains = standardDomainRepository.findAllByProjectIdAndCommonStandardDomainCategory(standardWord.getProjectId(), domainCategory);

            if(standardDomains.isEmpty()){
                throw new CustomException(ErrorCode.DOMAIN_IS_NOT_DEFINED, domainCategory);
            }
        }
    }

    @Transactional
    public void updateStandardWords(StandardWordDto standardWordDto) {

        List<String> restrictedWordList = new ArrayList<>();
        String restrictedWords = standardWordDto.getRestrictedWords();

        if(!restrictedWords.isEmpty()) {
            restrictedWordList.addAll(Arrays.asList(restrictedWords.split(",")));
        }

        StandardWord standardWord = standardWordRepository.findByIdAndProjectId(standardWordDto.getId(), standardWordDto.getProjectId());

        standardWord.setUseAbbreviation(standardWordDto.getUseAbbreviation().equals("Y"));
        standardWord.setRevisionNumber(standardWordDto.getRevisionNumber());
        standardWord.setCommonStandardWordDescription(standardWordDto.getCommonStandardWordDescription());
        standardWord.setIsFormatWord(standardWordDto.getIsFormatWord().equals("Y"));
        standardWord.setRestrictedWords(restrictedWordList);
    }
}
