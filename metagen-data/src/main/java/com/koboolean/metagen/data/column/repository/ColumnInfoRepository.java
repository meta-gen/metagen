package com.koboolean.metagen.data.column.repository;

import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ColumnInfoRepository extends JpaRepository<ColumnInfo, Long> {
    Page<ColumnInfo> findAllByProjectId(Long projectId, Pageable pageable);

    Page<ColumnInfo> findByProjectId(Long projectId, Pageable pageable);


    /** 같은 테이블 안의 같은 컬럼명 (컬럼 등록 중복 기준) */
    List<ColumnInfo> findAllByProjectIdAndTableInfo_IdAndColumnName(Long projectId, Long tableInfoId, String columnName);

    Page<ColumnInfo> findAllByProjectIdAndTableNameLike(Long projectId, String tableName, Pageable pageable);
    Page<ColumnInfo> findAllByProjectIdAndColumnNameLikeIgnoreCase(Long projectId, String columnName, Pageable pageable);

    Page<ColumnInfo> findAllByProjectIdAndColumnDescLike(Long projectId, String columnDesc, Pageable pageable);

    Page<ColumnInfo> findAllByProjectIdAndDataTypeLike(Long projectId, String dataType, Pageable pageable);

    ColumnInfo findByProjectIdAndId(Long projectId, Long id);

    List<ColumnInfo> findAllByProjectIdAndId(Long projectId, Long id);

    ColumnInfo findByIdAndProjectId(Long id, Long projectId);

    List<ColumnInfo> findAllByProjectIdAndRefTableName(Long projectId, String refTableName);

    /** 엑셀 업로드 : 청크에서 이미 등록된 (테이블 ID, 컬럼명) 조합을 한 번에 조회 → [tableInfoId, columnName] */
    @Query("select c.tableInfo.id, c.columnName from ColumnInfo c " +
           "where c.projectId = :projectId and c.tableInfo.id in :tableInfoIds and c.columnName in :columnNames")
    List<Object[]> findTableColumnKeys(@Param("projectId") Long projectId,
                                       @Param("tableInfoIds") Collection<Long> tableInfoIds,
                                       @Param("columnNames") Collection<String> columnNames);
}
