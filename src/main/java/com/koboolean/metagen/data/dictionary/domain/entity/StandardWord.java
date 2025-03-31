package com.koboolean.metagen.data.dictionary.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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

    private boolean isFormatWord; // 형식단어 여부 (true/false)

    private String commonStandardDomainCategory; // 표준도메인분류명

    @ElementCollection
    private List<String> synonymList; // 이음동의어 목록

    @ElementCollection
    private List<String> restrictedWords; // 금칙어 목록

    private Boolean isApproval;

    @OneToMany(mappedBy = "standardWord", cascade = CascadeType.ALL)
    private List<StandardTermWordMapping> termWordMappings;
}
