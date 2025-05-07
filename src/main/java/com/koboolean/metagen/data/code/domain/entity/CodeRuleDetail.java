package com.koboolean.metagen.data.code.domain.entity;

import com.koboolean.metagen.home.jpa.BaseEntity;
import com.koboolean.metagen.system.code.domain.entity.CodeRule;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeRuleDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    private String functionGroup; // 그룹명 (ex: 회원관리, 게시판 등)

    private String description; // 기타 설명

    private String methodName; // 변환된 메소드 명

    private String swaggerData; // 스웨거 변환 데이터

    private String methodKeyword;   // ${1} - 사용자 입력 기반, 표준용어 조합 키워드

    private String methodPurpose;   // ${2} - 메소드의 기능이나 목적 설명

    private Boolean isDicAbbrUsed;  // true면 약어 기준, false면 영문명 기준

    private Boolean useSwagger;

    private String input;

    private String output;

    private String exception;

    @ManyToOne
    @JoinColumn(name = "code_rule_id")
    private CodeRule codeRule; // 코드규칙
}
