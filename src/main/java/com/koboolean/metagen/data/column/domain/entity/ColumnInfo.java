package com.koboolean.metagen.data.column.domain.entity;

import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.table.domain.entity.TableInfo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "column_info")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ColumnInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "column_info_id", nullable = false, unique = true)
    private Long id;

    @Column
    private Long projectId;

    /** 테이블명: ex_table */
    @Column
    private String tableName;

    /** 컬럼명: email */
    @Column
    private String columnName;

    /** 컬럼 설명: 이메일 */
    @Column
    private String columnDesc;

    /** 데이터 타입: VARCHAR2 */
    @Column
    private String dataType;

    /** 최대 길이: nan */
    @Column
    private Integer maxLength;

    /** 정밀도: nan */
    @Column
    private BigDecimal precision;

    /** 소수 자릿수: nan */
    @Column
    private Integer scale;

    /** NULL 허용 여부: N */
    @Column
    private Boolean isNullable;

    /** 기본값: nan */
    @Column
    private String defaultValue;

    /** 정렬 순서: 1 */
    @Column
    private Integer sortOrder;

    /** 마스터 데이터 여부: N */
    @Column
    private Boolean isMasterData;

    /** 참조 테이블명: nan */
    @Column
    private String refTableName;

    /** 필수 입력 여부: Y */
    @Column
    private Boolean isRequired;

    /** 민감정보 여부: Y */
    @Column
    private Boolean isSensitive;

    /** 고유값 여부: Y */
    @Column
    private Boolean isUnique;

    /** 인덱스 생성 여부: Y */
    @Column
    private Boolean isIndex;

    /** 암호화 필요 여부: N */
    @Column
    private Boolean isEncrypted;

    /** 데이터 예시: test@test.com */
    @Column
    private String example;

    /** 승인여부 : N **/
    @Column
    private Boolean isApproval;

    @ManyToMany
    @JoinTable(
            name = "table_design_standard_term",
            joinColumns = @JoinColumn(name = "column_info_id"),
            inverseJoinColumns = @JoinColumn(name = "standard_term_id")

    )
    private List<StandardTerm> standardTerms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_info_id")
    private TableInfo tableInfo;

}
