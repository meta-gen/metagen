package com.koboolean.metagen.data.column.service;

import com.koboolean.metagen.data.column.domain.dto.ColumnInfoDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ColumnManageService {
    List<ColumnDto> selectTableColumn();

    Page<ColumnInfoDto> selectTableData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery, Long tableId);

    List<TableInfoDto> selectTable(AccountDto accountDto, String tableName);

    List<ColumnInfoDto> selectTableColumn(AccountDto accountDto, Long tableId);

    void insertColumn(AccountDto accountDto, ColumnInfoDto columnInfoDto);

    List<StandardTermDto> selectTermData(AccountDto accountDto, String type, String data);

    void updateColumnApproval(AccountDto accountDto, List<ColumnInfoDto> columnInfoDtos, String type);

    void deleteColumn(AccountDto accountDto, List<ColumnInfoDto> columnInfoDtos);
}
