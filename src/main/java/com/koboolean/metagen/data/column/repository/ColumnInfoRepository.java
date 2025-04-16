package com.koboolean.metagen.data.column.repository;

import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnInfoRepository extends JpaRepository<ColumnInfo, Long> {
    Page<ColumnInfo> findAllByProjectId(Long projectId, Pageable pageable);

    Page<ColumnInfo> findByProjectId(Long projectId, Pageable pageable);

    List<ColumnInfo> findAllByProjectIdAndColumnName(Long projectId, String columnName);

    Page<ColumnInfo> findAllByProjectIdAndTableNameLike(Long projectId, String tableName, Pageable pageable);
    Page<ColumnInfo> findAllByProjectIdAndColumnNameLike(Long projectId, String columnName, Pageable pageable);

    Page<ColumnInfo> findAllByProjectIdAndColumnDescLike(Long projectId, String columnDesc, Pageable pageable);

    Page<ColumnInfo> findAllByProjectIdAndDataTypeLike(Long projectId, String dataType, Pageable pageable);
}
