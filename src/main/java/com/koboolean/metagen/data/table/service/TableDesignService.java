package com.koboolean.metagen.data.table.service;

import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TableDesignService {

    List<ColumnDto> selectTableInfoColumn();

    Page<TableInfoDto> selectTableInfoData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery);

    void saveTable(TableInfoDto tableInfoDto, AccountDto accountDto);

    void updateTable(TableInfoDto tableInfoDto, AccountDto accountDto);

    void updateTableIsApproval(boolean isApproval, AccountDto accountDto, List<TableInfoDto> tableInfoDtos);

    void deleteTable(List<TableInfoDto> tableInfoDtos, AccountDto accountDto);
}
