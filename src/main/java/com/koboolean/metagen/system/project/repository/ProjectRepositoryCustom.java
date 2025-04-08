package com.koboolean.metagen.system.project.repository;

import com.koboolean.metagen.system.project.domain.entity.Project;

import java.util.List;

public interface ProjectRepositoryCustom {
    List<Project> findProjectsByUsername(String username);
}
