package com.koboolean.metagen.system.project.domain.entity;

import com.koboolean.metagen.home.jpa.BaseEntity;
import com.koboolean.metagen.security.domain.entity.Account;
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
    private List<ProjectMember> projectMember = new ArrayList<>();

    @Column(name = "is_auto_active")
    private Boolean isAutoActive; // 자동 승인여부

    @Column(name = "is_dic_abbr_used")
    private Boolean isDicAbbrUsed; // 데이터 사전 약어 사용 여부

    @OneToMany
    private List<Template> templateType;
}
