package com.koboolean.metagen.data.dictionary.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "standard_term_word_mappings")
public class StandardTermWordMapping {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "standard_term_id")
    private StandardTerm standardTerm;

    @ManyToOne
    @JoinColumn(name = "standard_word_id")
    private StandardWord standardWord;

    private int orderIndex; // 사용자_이름 → 사용자(1), 이름(2)
}
