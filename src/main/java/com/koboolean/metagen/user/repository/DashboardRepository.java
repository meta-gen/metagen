package com.koboolean.metagen.user.repository;

import com.koboolean.metagen.system.project.domain.entity.Project;
import com.koboolean.metagen.user.repository.impl.DashboardRepositoryImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Project, Long>, DashboardRepositoryCustom {
}
