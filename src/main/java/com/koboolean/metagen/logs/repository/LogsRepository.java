package com.koboolean.metagen.logs.repository;

import com.koboolean.metagen.logs.domain.entity.Logs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogsRepository extends JpaRepository<Logs, Long> {
}
