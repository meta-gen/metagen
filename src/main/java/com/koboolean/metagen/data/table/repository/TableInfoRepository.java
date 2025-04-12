package com.koboolean.metagen.data.table.repository;

import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.data.table.domain.entity.TableInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableInfoRepository extends JpaRepository<TableInfo, Long> {
    Page<TableInfo> findAllByProjectId(Long projectId, Pageable pageable);

    TableInfo findByIdAndProjectId(Long id, Long projectId);

    List<TableInfo> findAllByIdAndProjectId(Long id, Long projectId);

    List<TableInfo> findAllByTableNameAndProjectId(String tableName, Long projectId);

    Page<TableInfo> findAllByProjectIdAndTableNameLike(Long projectId, String tableName, Pageable pageable);

    Page<TableInfo> findAllByProjectIdAndTableDescriptionLike(Long projectId, String tableDescription, Pageable pageable);
}
