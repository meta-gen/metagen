package com.koboolean.metagen.data.column.domain.dto;

import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnInfoDto {

    /** 식별자 (PK) */
    private Long id;

    /** 프로젝트 ID **/
    private Long projectId;

    private Long termId;
    private Long tableInfoId;

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

    /** PK 여부 */
    private String isPk;

    /** 고유값 여부 */
    private String isUnique;

    /** 인덱스 생성 여부 */
    private String isIndex;

    /** 암호화 필요 여부 */
    private String isEncrypted;

    /** 데이터 예시 */
    private String example;

    /** 연결된 표준 단어 리스트 */
    private StandardTermDto standardTerms;

    /** 승인여부 **/
    private String isApproval;

    public static ColumnInfoDto fromEntity(ColumnInfo entity) {
        return ColumnInfoDto.builder()
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
                .isPk(toYN(entity.getIsPk()))
                .isRequired(toYN(entity.getIsPk() || entity.getIsRequired()))
                .isSensitive(toYN(entity.getIsSensitive()))
                .isUnique(toYN(entity.getIsPk() || entity.getIsUnique()))
                .isIndex(toYN(entity.getIsIndex()))
                .isEncrypted(toYN(entity.getIsEncrypted()))
                .example(entity.getExample())
                .standardTerms(StandardTermDto.fromEntity(entity.getStandardTerms()))
                .isApproval(toYN(entity.getIsApproval()))
                .build();
    }

    public ColumnInfo toEntity() {
        return ColumnInfo.builder()
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
                .isPk(fromYN(this.isPk))
                .isRequired(fromYN(this.isPk) || fromYN(this.isRequired))
                .isSensitive(fromYN(this.isSensitive))
                .isUnique(fromYN(this.isPk) || fromYN(this.isUnique))
                .isIndex(fromYN(this.isIndex))
                .isEncrypted(fromYN(this.isEncrypted))
                .example(this.example)
                .isApproval(fromYN(this.isApproval))
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
