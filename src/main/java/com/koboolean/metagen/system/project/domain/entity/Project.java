package com.koboolean.metagen.system.project.domain.entity;

import com.koboolean.metagen.board.domain.entity.Board;
import com.koboolean.metagen.home.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity implements Serializable {

    @Id @GeneratedValue
    @Column(name = "project_id")
    private Long id;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "project")
    private List<ProjectMember> projectMembers = new ArrayList<>();

}
