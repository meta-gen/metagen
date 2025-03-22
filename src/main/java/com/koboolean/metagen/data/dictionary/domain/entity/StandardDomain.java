package com.koboolean.metagen.data.dictionary.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "standard_domains")
public class StandardDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 번호

    @Column(name = "project_id")
    private Long projectId;

    private int revisionNumber; // 제정차수

    private String commonStandardDomainGroupName; // 표준도메인그룹명

    private String commonStandardDomainCategory; // 표준도메인분류명

    private String commonStandardDomainName; // 표준도메인명

    @Column(length = 1000)
    private String commonStandardDomainDescription; // 표준도메인설명

    private String dataType; // 데이터타입

    private Integer dataLength; // 데이터길이

    private Integer dataDecimalLength; // 데이터소수점길이

    private String storageFormat; // 저장 형식

    private String displayFormat; // 표현 형식

    private String unit; // 단위

    @Column(length = 2000)
    private String allowedValues; // 허용값

    private Boolean isApproval;
}
