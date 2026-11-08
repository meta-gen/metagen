package com.koboolean.metagen.data.dictionary.domain.entity;
import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import com.koboolean.metagen.common.jpa.BaseEntity;
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
public class StandardTerm extends BaseEntity {

    @Id
    // 시퀀스(pooled, 50개씩 할당) : IDENTITY 는 JDBC 배치 INSERT 가 꺼지므로 사용하지 않는다 (V6 마이그레이션)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "standard_terms_seq")
    @SequenceGenerator(name = "standard_terms_seq", sequenceName = "standard_terms_seq", allocationSize = 50)
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "standard_domain_id")
    private StandardDomain standardDomain;

    // 이 용어를 사용하는 컬럼 : ColumnInfo.standardTerms(FK) 기준 읽기 전용 (조인 테이블은 V6 에서 제거)
    @OneToMany(mappedBy = "standardTerms")
    private List<ColumnInfo> columnInfos;
}
