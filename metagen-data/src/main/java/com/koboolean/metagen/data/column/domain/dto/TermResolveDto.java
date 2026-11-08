package com.koboolean.metagen.data.column.domain.dto;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;

import java.util.List;

/**
 * 붙여넣은 용어명/영문약어명 한 줄을 표준용어로 찾은 결과 (컬럼 일괄 편집 > 붙여넣기로 추가)
 *
 * @param input  입력한 값 (그대로)
 * @param status MATCHED(1건) / AMBIGUOUS(여러 건 : 같은 용어명의 다른 도메인 등) / NOT_FOUND
 * @param terms  찾은 표준용어
 */
public record TermResolveDto(String input, String status, List<StandardTermDto> terms) {

    public static final String MATCHED = "MATCHED";
    public static final String AMBIGUOUS = "AMBIGUOUS";
    public static final String NOT_FOUND = "NOT_FOUND";

    public static TermResolveDto of(String input, List<StandardTermDto> terms) {
        String status = terms.isEmpty() ? NOT_FOUND : terms.size() == 1 ? MATCHED : AMBIGUOUS;
        return new TermResolveDto(input, status, terms);
    }
}
