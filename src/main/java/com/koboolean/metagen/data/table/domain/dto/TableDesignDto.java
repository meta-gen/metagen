package com.koboolean.metagen.data.table.domain.dto;

import com.koboolean.metagen.data.table.domain.entity.TableDesign;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableDesignDto {

    /** 식별자 (PK) */
    private Long id;

    /** 프로젝트 ID **/
    private Long projectId;

    /** 테이블명 */
    private String tableName;

    /** 컬럼명 */
    private String columnName;

    /** 컬럼 설명 */
    private String columnDesc;

    /** 데이터 타입 */
    private String dataType;

    /** 최대 길이 */
    private Integer maxLength;

    /** 정밀도 */
    private BigDecimal precision;

    /** 소수 자릿수 */
    private Integer scale;

    /** NULL 허용 여부 */
    private String isNullable;

    /** 기본값 */
    private String defaultValue;

    /** 정렬 순서 */
    private Integer sortOrder;

    /** 마스터 데이터 여부 */
    private String isMasterData;

    /** 참조 테이블명 */
    private String refTableName;

    /** 필수 입력 여부 */
    private String isRequired;

    /** 민감정보 여부 */
    private String isSensitive;

    /** 고유값 여부 */
    private String isUnique;

    /** 인덱스 생성 여부 */
    private String isIndex;

    /** 암호화 필요 여부 */
    private String isEncrypted;

    /** 엑셀 컬럼 헤더 표시명 */
    private String excelHeader;

    /** 데이터 예시 */
    private String example;

    /** 연결된 표준 단어 리스트 */
    private List<StandardTerm> standardTerms;

    public static TableDesignDto fromEntity(TableDesign entity) {
        return TableDesignDto.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .tableName(entity.getTableName())
                .columnName(entity.getColumnName())
                .columnDesc(entity.getColumnDesc())
                .dataType(entity.getDataType())
                .maxLength(entity.getMaxLength())
                .precision(entity.getPrecision())
                .scale(entity.getScale())
                .isNullable(toYN(entity.getIsNullable()))
                .defaultValue(entity.getDefaultValue())
                .sortOrder(entity.getSortOrder())
                .isMasterData(toYN(entity.getIsMasterData()))
                .refTableName(entity.getRefTableName())
                .isRequired(toYN(entity.getIsRequired()))
                .isSensitive(toYN(entity.getIsSensitive()))
                .isUnique(toYN(entity.getIsUnique()))
                .isIndex(toYN(entity.getIsIndex()))
                .isEncrypted(toYN(entity.getIsEncrypted()))
                .excelHeader(entity.getExcelHeader())
                .example(entity.getExample())
                .standardTerms(entity.getStandardTerms())
                .build();
    }

    public TableDesign toEntity() {
        return TableDesign.builder()
                .id(this.id)
                .projectId(this.projectId)
                .tableName(this.tableName)
                .columnName(this.columnName)
                .columnDesc(this.columnDesc)
                .dataType(this.dataType)
                .maxLength(this.maxLength)
                .precision(this.precision)
                .scale(this.scale)
                .isNullable(fromYN(this.isNullable))
                .defaultValue(this.defaultValue)
                .sortOrder(this.sortOrder)
                .isMasterData(fromYN(this.isMasterData))
                .refTableName(this.refTableName)
                .isRequired(fromYN(this.isRequired))
                .isSensitive(fromYN(this.isSensitive))
                .isUnique(fromYN(this.isUnique))
                .isIndex(fromYN(this.isIndex))
                .isEncrypted(fromYN(this.isEncrypted))
                .excelHeader(this.excelHeader)
                .example(this.example)
                .standardTerms(this.standardTerms)
                .build();
    }

    private static String toYN(Boolean bool) {
        return bool == null ? null : (bool ? "Y" : "N");
    }

    private static Boolean fromYN(String yn) {
        if (yn == null) return null;
        return yn.equalsIgnoreCase("Y");
    }
}
