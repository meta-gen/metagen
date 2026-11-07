package com.koboolean.metagen.data.table.service;

import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.data.table.domain.dto.TableNameDto;
import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface TableDesignService {

    List<ColumnDto> selectTableInfoColumn();

    Page<TableInfoDto> selectTableInfoData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery);

    /**
     * 테이블 한글명(예: 사용_내역_관리)을 승인된 표준 단어의 영문약어로 변환한다.
     */
    TableNameDto generateTableName(String tableKorName, AccountDto accountDto);

    void saveTable(TableInfoDto tableInfoDto, AccountDto accountDto);

    void updateTable(TableInfoDto tableInfoDto, AccountDto accountDto);

    void updateTableIsApproval(boolean isApproval, AccountDto accountDto, List<TableInfoDto> tableInfoDtos);

    void deleteTable(List<TableInfoDto> tableInfoDtos, AccountDto accountDto);

    void uploadTableExcelFile(MultipartFile file, AccountDto accountDto) throws IOException;
}
