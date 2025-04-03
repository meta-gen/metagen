package com.koboolean.metagen.system.project.domain.entity;

import com.koboolean.metagen.home.jpa.BaseEntity;
import com.koboolean.metagen.security.domain.entity.Account;
import com.koboolean.metagen.system.project.domain.enums.TemplateType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity implements Serializable {

    @Id @GeneratedValue
    @Column(name = "project_id")
    private Long id;

    @Column(name = "project_name")
    private String projectName;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "project")
    private List<ProjectMember> projectMembers = new ArrayList<>();

    @Column(name = "is_auto_active")
    private Boolean isAutoActive; // 자동 승인여부

    @Column(name = "is_use_swagger")
    private Boolean isUseSwagger;

    @Column(name = "template_type")
    @Enumerated(EnumType.STRING)
    private List<TemplateType> templateType;
}
