package com.koboolean.metagen.home.repository;

import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.home.repository.impl.DashboardRepositoryImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Project, Long>, DashboardRepositoryCustom {
}
