package com.koboolean.metagen.security.domain.entity;

import com.koboolean.metagen.home.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity implements Serializable {

    @Id @GeneratedValue
    private Long id;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "is_active")
    private Boolean isActive;

}
