package com.koboolean.metagen.data.column.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 컬럼 일괄 편집 저장 요청 (테이블 관리 > 테이블 상세 > 컬럼 편집)
 * - rows : 화면에 보이는 순서대로의 컬럼 행 (기존 컬럼은 id, 새 컬럼은 termId). 이 순서로 정렬 순서를 다시 매긴다.
 * - deletedIds : 삭제할 기존 컬럼 ID (승인 전 컬럼만)
 * 승인된 컬럼은 행에 포함되더라도 내용/순서를 바꾸지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnBulkSaveDto {

    private Long tableInfoId;

    @Builder.Default
    private List<Row> rows = new ArrayList<>();

    @Builder.Default
    private List<Long> deletedIds = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Row {
        /** 화면 행 식별자 : 오류를 해당 행에 표시하기 위해 그대로 돌려준다 */
        private String key;

        /** 기존 컬럼 ID (새 컬럼이면 null) */
        private Long id;

        /** 새 컬럼의 표준용어 ID : 컬럼명은 이 용어의 영문약어명으로 정한다 */
        private Long termId;

        private String columnDesc;
        private String dataType;
        private Integer maxLength;
        private BigDecimal precision;
        private Integer scale;
        private String defaultValue;
        private String refTableName;
        private String example;

        /** Y / N */
        private String isPk;
        private String isNullable;
        private String isRequired;
        private String isUnique;
        private String isIndex;
        private String isMasterData;
        private String isSensitive;
        private String isEncrypted;
    }

    /**
     * 저장 결과 : 검증 오류가 있으면 아무것도 저장하지 않고 errors 를 돌려준다.
     */
    public record Result(boolean result, int created, int updated, int deleted, List<RowError> errors) {
        public static Result fail(List<RowError> errors) {
            return new Result(false, 0, 0, 0, errors);
        }
    }

    /**
     * @param key     화면 행 식별자 (행과 무관한 오류면 null)
     * @param message 오류 내용
     */
    public record RowError(String key, String message) {}
}
