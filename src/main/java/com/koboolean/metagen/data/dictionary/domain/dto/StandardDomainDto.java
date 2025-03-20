package com.koboolean.metagen.data.dictionary.domain.dto;

import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardDomainDto {
    private Long id; // 번호
    private Long projectId;
    private int revisionNumber; // 제정차수
    private String commonStandardDomainGroupName; // 표준도메인그룹명
    private String commonStandardDomainCategory; // 표준도메인분류명
    private String commonStandardDomainName; // 표준도메인명
    private String commonStandardDomainDescription; // 표준도메인설명
    private String dataType; // 데이터타입
    private Integer dataLength; // 데이터길이
    private Integer dataDecimalLength; // 데이터소수점길이
    private String storageFormat; // 저장 형식
    private String displayFormat; // 표현 형식
    private String unit; // 단위
    private String allowedValues; // 허용값

    public static StandardDomainDto fromEntity(StandardDomain entity) {
        return StandardDomainDto.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .revisionNumber(entity.getRevisionNumber())
                .commonStandardDomainGroupName(entity.getCommonStandardDomainGroupName())
                .commonStandardDomainCategory(entity.getCommonStandardDomainCategory())
                .commonStandardDomainName(entity.getCommonStandardDomainName())
                .commonStandardDomainDescription(entity.getCommonStandardDomainDescription())
                .dataType(entity.getDataType())
                .dataLength(entity.getDataLength())
                .dataDecimalLength(entity.getDataDecimalLength())
                .storageFormat(entity.getStorageFormat())
                .displayFormat(entity.getDisplayFormat())
                .unit(entity.getUnit())
                .allowedValues(entity.getAllowedValues())
                .build();
    }
}
