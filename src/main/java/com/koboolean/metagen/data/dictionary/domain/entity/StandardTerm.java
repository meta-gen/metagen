package com.koboolean.metagen.data.dictionary.domain.entity;
import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "standard_terms")
public class StandardTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "standard_term_id")
    private Long id; // 번호

    @Column(name = "project_id")
    private Long projectId;

    private int revisionNumber; // 제정차수

    private String commonStandardTermName; // 표준용어명

    @Column(length = 1000)
    private String commonStandardTermDescription; // 표준용어설명

    @OneToMany(mappedBy = "standardTerm", cascade = CascadeType.ALL)
    private List<StandardTermWordMapping> termWordMappings;

    private String commonStandardTermAbbreviation;

    private String administrativeStandardCodeName; // 행정표준코드명

    private String responsibleOrganization; // 소관기관명

    @ElementCollection
    private List<String> synonymList; // 용어 이음동의어 목록

    private Boolean isApproval;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_domain_id")
    private StandardDomain standardDomain;

    @OneToMany
    private List<ColumnInfo> columnInfos;
}
