package com.koboolean.metagen.data.column.repository;

import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColumnInfoRepository extends JpaRepository<ColumnInfo, Long> {
    Page<ColumnInfo> findAllByProjectId(Long projectId, Pageable pageable);

    Page<ColumnInfo> findByTableInfo_IdAndProjectId(Long tableId, Long projectId, Pageable pageable);
}
