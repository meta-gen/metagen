package com.koboolean.metagen.system.project.domain.entity;

import com.koboolean.metagen.home.jpa.BaseEntity;
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

    @OneToMany
    private List<CodeRule> codeRules;

}
