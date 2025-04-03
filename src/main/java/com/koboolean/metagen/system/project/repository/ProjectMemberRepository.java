package com.koboolean.metagen.system.project.repository;

import com.koboolean.metagen.system.project.domain.entity.ProjectMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    Page<ProjectMember> findAllByProject_Id(Pageable pageable, Long projectId);
    List<ProjectMember> findAllByProject_Id(Long projectId);

    Page<ProjectMember> findAllByIsActiveAndProject_id(Pageable pageable, boolean y, Long projectId);
}
