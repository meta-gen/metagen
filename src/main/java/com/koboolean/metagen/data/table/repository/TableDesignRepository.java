package com.koboolean.metagen.data.table.repository;

import com.koboolean.metagen.data.table.domain.entity.TableDesign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableDesignRepository extends JpaRepository<TableDesign, Long> {
    Page<TableDesign> findAllByProjectId(Long projectId, Pageable pageable);
}
