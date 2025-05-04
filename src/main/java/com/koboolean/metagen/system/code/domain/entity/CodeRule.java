package com.koboolean.metagen.system.code.domain.entity;

import com.koboolean.metagen.data.code.domain.entity.CodeRuleDetail;
import com.koboolean.metagen.home.jpa.BaseEntity;
import com.koboolean.metagen.system.project.domain.entity.Template;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeRule extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "code_rule_id")
    private Long id;

    private Long projectId;

    /** 조회용 template Name **/
    private String templateName;

    private String codeRuleName;

    private String codeRuleDescription;

    private String prefix;

    private String suffix;

    private String methodForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    private String input;

    private String output;

    @OneToMany(mappedBy = "codeRule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodeRuleDetail> codeRuleDetails;
}
