package com.koboolean.metagen.data.table.domain.entity;

import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import com.koboolean.metagen.home.jpa.BaseEntity;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
