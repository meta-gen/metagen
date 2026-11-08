package com.koboolean.metagen.data.table.domain.entity;

import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import com.koboolean.metagen.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "table_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableInfo extends BaseEntity {

    @Id
    // 시퀀스(pooled, 50개씩 할당) : IDENTITY 는 JDBC 배치 INSERT 가 꺼지므로 사용하지 않는다 (V6 마이그레이션)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "table_info_seq")
    @SequenceGenerator(name = "table_info_seq", sequenceName = "table_info_seq", allocationSize = 50)
    @Column(name = "table_info_id", nullable = false, unique = true)
    private Long id;

    /** 프로젝트 ID */
    @Column(nullable = false)
    private Long projectId;

    /** 테이블명: ex_table */
    @Column(nullable = false)
    private String tableName;

    /** 테이블 설명 */
    @Column
    private String tableDescription;

    /** 테이블 한글명: 사용 내역 관리 (표준 단어 기준 테이블명 생성에 사용한 단어) */
    @Column
    private String tableKorName;

    /** 마스터 여부 */
    @Column
    private Boolean isMasterTable;

    /** 정렬 순서 */
    @Column
    private Integer sortOrder;

    /** 테이블 승인 여부 */
    @Column
    private Boolean isApproval;

    @OneToMany(mappedBy = "tableInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColumnInfo> columns = new ArrayList<>();
}
