package com.koboolean.metagen.data.commoncolumn.domain.entity;

import com.koboolean.metagen.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 프로젝트 공통 컬럼 세트의 한 항목 (컬럼 일괄 편집 > 공통 컬럼 추가)
 * 컬럼명/설명/데이터 타입은 표준용어에서 가져오고, 여기에는 순서와 NULL 허용/기본값/인덱스만 둔다.
 * 표준용어가 지워질 수 있으므로 용어는 ID 로만 참조한다. (조회 시 없는 용어는 "용어 없음"으로 표시)
 */
@Entity
@Table(name = "common_column")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonColumn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "common_column_seq")
    @SequenceGenerator(name = "common_column_seq", sequenceName = "common_column_seq", allocationSize = 50)
    @Column(name = "common_column_id")
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_nullable")
    private Boolean isNullable;

    @Column(name = "is_index")
    private Boolean isIndex;

    @Column(name = "default_value")
    private String defaultValue;
}
