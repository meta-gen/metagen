package com.koboolean.metagen.logs.repository;

import com.koboolean.metagen.logs.domain.entity.Logs;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogsRepository extends JpaRepository<Logs, Long> {

    Page<Logs> findAllByProjectId(Long projectId, Pageable pageable);
}
