package com.koboolean.metagen.data.dictionary.service;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardWordRepository;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StandardWordService {

    private static final int FIRST_ROW = 1;
    private final StandardWordRepository standardWordRepository;

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

            List<StandardWord> nameIsDupl = standardWordRepository.findAllByProjectIdAndCommonStandardWordName(standardWord.getProjectId(), standardWord.getCommonStandardWordName());

            if(!nameIsDupl.isEmpty()){
                throw new CustomException(ErrorCode.SAVED_DATA_EXISTS);
            }

            standardWordRepository.save(standardWord);
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
            standardWordRepository.save(standardWord);
        }else{
            throw new CustomException(ErrorCode.SAVED_DATA_EXISTS);
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

        standardWord.setRevisionNumber(standardWordDto.getRevisionNumber());
        standardWord.setCommonStandardWordDescription(standardWordDto.getCommonStandardWordDescription());
        standardWord.setIsFormatWord(standardWordDto.getIsFormatWord().equals("Y"));
        standardWord.setRestrictedWords(restrictedWordList);
    }
}
