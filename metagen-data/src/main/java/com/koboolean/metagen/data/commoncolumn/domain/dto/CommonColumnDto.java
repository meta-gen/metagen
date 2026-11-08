package com.koboolean.metagen.data.commoncolumn.domain.dto;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 공통 컬럼 세트 항목
 * - 저장 요청 : termId, isNullable, defaultValue, isIndex (순서는 목록 순서)
 * - 조회 응답 : 위 값 + term (표준용어, 지워졌으면 null)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonColumnDto {

    private Long id;

    private Long termId;

    private Integer sortOrder;

    /** Y / N */
    private String isNullable;

    /** Y / N */
    private String isIndex;

    private String defaultValue;

    /** 표준용어 (컬럼명/설명/데이터 타입). 용어가 지워졌으면 null */
    private StandardTermDto term;
}
