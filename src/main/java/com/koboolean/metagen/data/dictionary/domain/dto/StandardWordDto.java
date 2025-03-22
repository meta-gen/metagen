package com.koboolean.metagen.data.dictionary.domain.dto;

import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardWordDto {
    private Long id; // 번호
    private Long projectId;
    private int revisionNumber; // 제정차수
    private String commonStandardWordName; // 표준단어명
    private String commonStandardWordAbbreviation; // 표준단어영문약어명
    private String commonStandardWordEnglishName; // 표준단어 영문명
    private String commonStandardWordDescription; // 표준단어 설명
    private String isFormatWord; // 형식단어 여부
    private String commonStandardDomainCategory; // 표준도메인분류명
    private String synonyms;
    private String restrictedWords;
    private String isApprovalYn;

    public static StandardWordDto fromEntity(StandardWord entity) {
        return StandardWordDto.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .revisionNumber(entity.getRevisionNumber())
                .commonStandardWordName(entity.getCommonStandardWordName())
                .commonStandardWordAbbreviation(entity.getCommonStandardWordAbbreviation())
                .commonStandardWordEnglishName(entity.getCommonStandardWordEnglishName())
                .commonStandardWordDescription(entity.getCommonStandardWordDescription())
                .isFormatWord(entity.isFormatWord() ? "Y" : "N")
                .commonStandardDomainCategory(entity.getCommonStandardDomainCategory())
                .synonyms(convertListToString(entity.getSynonymList()))
                .restrictedWords(convertListToString(entity.getRestrictedWords()))
                .isApprovalYn(entity.getIsApproval() ? "Y" : "N")
                .build();
    }

    private static String convertListToString(List<String> list) {
        return (list == null || list.isEmpty()) ? "" : list.stream().collect(Collectors.joining(","));
    }
}
