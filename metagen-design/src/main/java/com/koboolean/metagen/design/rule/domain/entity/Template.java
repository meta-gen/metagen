package com.koboolean.metagen.design.rule.domain.entity;

import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.common.jpa.BaseEntity;
import com.koboolean.metagen.design.rule.domain.entity.CodeRule;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Template extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "template_id")
    private Long id;

    private Long projectId;

    private String templateName;

    private String templateDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodeRule> codeRules;

}
