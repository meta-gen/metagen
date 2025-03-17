package com.koboolean.metagen.user.repository;

import com.koboolean.metagen.security.domain.entity.Project;

import java.util.List;

public interface ProjectRepositoryCustom {
    List<Project> findProjectsByUsername(String username);
}
