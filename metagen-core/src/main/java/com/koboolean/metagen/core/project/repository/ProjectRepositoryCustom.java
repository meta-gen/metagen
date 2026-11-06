package com.koboolean.metagen.core.project.repository;

import com.koboolean.metagen.core.project.domain.entity.Project;

import java.util.List;

public interface ProjectRepositoryCustom {
    List<Project> findProjectsByUsername(String username);
}
