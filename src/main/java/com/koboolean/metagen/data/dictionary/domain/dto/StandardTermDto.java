package com.koboolean.metagen.data.dictionary.domain.dto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTermWordMapping;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import lombok.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardTermDto {
    private Long id; // 번호
    private Long projectId;
    private int revisionNumber; // 제정차수
    private String commonStandardTermName; // 표준용어명
    private String commonStandardTermDescription; // 표준용어설명
    private String commonStandardTermAbbreviation; // 표준용어영문약어명
    private String commonStandardDomainName; // 표준도메인명
    private String allowedValues; // 허용값
    private String storageFormat; // 저장 형식
    private String displayFormat; // 표현 형식
    private String administrativeStandardCodeName; // 행정표준코드명
    private String responsibleOrganization; // 소관기관명
    private String synonyms;
    private String isApprovalYn;

    public static StandardTermDto fromEntity(StandardTerm entity) {

        String abbreviation = entity.getTermWordMappings().stream()
                .sorted(Comparator.comparingInt(StandardTermWordMapping::getOrderIndex))
                .map(mapping -> mapping.getStandardWord().getCommonStandardWordAbbreviation())
                .collect(Collectors.joining("_"));

        StandardDomain domain = entity.getStandardDomain();

        return StandardTermDto.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .revisionNumber(entity.getRevisionNumber())
                .commonStandardTermName(entity.getCommonStandardTermName())
                .commonStandardTermDescription(entity.getCommonStandardTermDescription())
                .commonStandardTermAbbreviation(abbreviation)
                .commonStandardDomainName(domain != null ? domain.getCommonStandardDomainName() : null)
                .allowedValues(domain != null ? domain.getAllowedValues() : null)
                .storageFormat(domain != null ? domain.getStorageFormat() : null)
                .displayFormat(domain != null ? domain.getDisplayFormat() : null)
                .administrativeStandardCodeName(entity.getAdministrativeStandardCodeName())
                .responsibleOrganization(entity.getResponsibleOrganization())
                .synonyms(convertListToString(entity.getSynonymList()))
                .isApprovalYn(entity.getIsApproval() ? "Y" : "N")
                .build();
    }

    private static String convertListToString(List<String> list) {
        return (list == null || list.isEmpty()) ? "" : list.stream().collect(Collectors.joining(","));
    }
}
