package com.koboolean.metagen.data.column.service;

import com.koboolean.metagen.data.column.domain.dto.ColumnInfoDto;
import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ColumnManageService {
    List<ColumnDto> selectTableColumn();

    Page<ColumnInfoDto> selectTableData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery, Long tableId);

    List<TableInfoDto> selectTable(AccountDto accountDto, String tableName);
}
