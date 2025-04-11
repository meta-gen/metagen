package com.koboolean.metagen.data.table.domain.dto;

import com.koboolean.metagen.data.table.domain.entity.TableInfo;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableInfoDto {

    /** 테이블 ID */
    private Long id;

    /** 프로젝트 ID */
    private Long projectId;

    /** 테이블명 */
    private String tableName;

    /** 테이블 설명 */
    private String tableDescription;

    /** 마스터 테이블 여부 */
    private String isMasterTable;

    /** 정렬 순서 */
    private Integer sortOrder;

    /** 승인 여부 */
    private String isApproval;

    public static TableInfoDto fromEntity(TableInfo entity) {
        return TableInfoDto.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .tableName(entity.getTableName())
                .tableDescription(entity.getTableDescription())
                .isMasterTable(toYN(entity.getIsMasterTable()))
                .sortOrder(entity.getSortOrder())
                .isApproval(toYN(entity.getIsApproval()))
                .build();
    }

    public TableInfo toEntity() {
        return TableInfo.builder()
                .id(this.id)
                .projectId(this.projectId)
                .tableName(this.tableName)
                .tableDescription(this.tableDescription)
                .isMasterTable(fromYN(this.isMasterTable))
                .sortOrder(this.sortOrder)
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
