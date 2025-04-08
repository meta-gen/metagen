package com.koboolean.metagen.system.project.repository;

import com.koboolean.metagen.system.project.domain.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectRepositoryCustom {

    List<Project> findByIsActive(boolean isActive);
}
