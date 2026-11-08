package com.koboolean.metagen.data.commoncolumn.repository;

import com.koboolean.metagen.data.commoncolumn.domain.entity.CommonColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonColumnRepository extends JpaRepository<CommonColumn, Long> {

    List<CommonColumn> findAllByProjectIdOrderBySortOrderAscIdAsc(Long projectId);
}
