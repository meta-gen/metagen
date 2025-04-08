package com.koboolean.metagen.data.dictionary.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "standard_words")
public class StandardWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 번호

    @Column(name = "project_id")
    private Long projectId;

    private int revisionNumber; // 제정차수

    private String commonStandardWordName; // 표준단어명

    private String commonStandardWordAbbreviation; // 표준단어영문약어명

    private String commonStandardWordEnglishName; // 표준단어 영문명

    @Column(length = 1000)
    private String commonStandardWordDescription; // 표준단어 설명

    private Boolean isFormatWord; // 형식단어 여부 (true/false)

    private String commonStandardDomainCategory; // 표준도메인분류명

    @ElementCollection
    private List<String> synonymList; // 이음동의어 목록

    @ElementCollection
    private List<String> restrictedWords; // 금칙어 목록

    private Boolean isApproval;

    @OneToMany(mappedBy = "standardWord", cascade = CascadeType.ALL)
    private List<StandardTermWordMapping> termWordMappings;

    public static StandardWord fromEntity(StandardWordDto standardWordDto) {

        List<String> synonyms = new ArrayList<>();

        if(!standardWordDto.getSynonyms().isEmpty()){
            synonyms.addAll(Arrays.asList(standardWordDto.getSynonyms().split(",")));
        }

        List<String> restrictedWords = new ArrayList<>();
        if(!standardWordDto.getRestrictedWords().isEmpty()){
            restrictedWords.addAll(Arrays.asList(standardWordDto.getRestrictedWords().split(",")));
        }


        return StandardWord.builder()
                .projectId(standardWordDto.getProjectId())
                .revisionNumber(standardWordDto.getRevisionNumber())
                .commonStandardWordName(standardWordDto.getCommonStandardWordName())
                .commonStandardWordAbbreviation(standardWordDto.getCommonStandardWordAbbreviation())
                .commonStandardWordEnglishName(standardWordDto.getCommonStandardWordEnglishName())
                .commonStandardWordDescription(standardWordDto.getCommonStandardWordDescription())
                .isFormatWord(standardWordDto.getIsFormatWord().equals("Y"))
                .commonStandardDomainCategory(standardWordDto.getCommonStandardDomainCategory())
                .synonymList(synonyms)
                .restrictedWords(restrictedWords)
                .isApproval(standardWordDto.getIsApprovalYn().equals("Y"))
                .build();
    }
}
