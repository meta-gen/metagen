package com.koboolean.metagen.data.column.service;

import com.koboolean.metagen.data.column.domain.dto.TermResolveDto;
import com.koboolean.metagen.data.column.domain.dto.ColumnBulkSaveDto;
import com.koboolean.metagen.data.column.domain.dto.ColumnInfoDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ColumnManageService {
    List<ColumnDto> selectTableColumn();

    Page<ColumnInfoDto> selectTableData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery, Long tableId);

    List<TableInfoDto> selectTable(AccountDto accountDto, String tableName);

    List<ColumnInfoDto> selectTableColumn(AccountDto accountDto, Long tableId);

    void insertColumn(AccountDto accountDto, ColumnInfoDto columnInfoDto);

    List<StandardTermDto> selectTermData(AccountDto accountDto, String type, String data);

    /**
     * 같은 테이블에 같은 컬럼명이 이미 등록되어 있는지 확인한다. (등록 화면 실시간 중복 확인 - 등록 시 서버 중복 기준과 동일)
     * @return 같은 테이블에 같은 컬럼명으로 등록된 컬럼 목록 (없으면 빈 목록)
     */
    List<ColumnInfoDto> selectDuplicateColumns(AccountDto accountDto, Long tableInfoId, String columnName);

    void updateColumnApproval(AccountDto accountDto, List<ColumnInfoDto> columnInfoDtos, String type);

    void deleteColumn(AccountDto accountDto, List<ColumnInfoDto> columnInfoDtos);

    void uploadColumnExcelFile(MultipartFile file, AccountDto accountDto) throws IOException;

    List<ColumnInfoDto> selectColumnDetail(AccountDto accountDto, Long id);

    void updateSortOrder(AccountDto accountDto, List<ColumnInfoDto> columnInfoDtos);

    /**
     * 테이블 컬럼 일괄 저장 (추가 / 승인 전 컬럼 수정 / 삭제 / 정렬 순서). 검증 오류가 하나라도 있으면 아무것도 저장하지 않는다.
     */
    ColumnBulkSaveDto.Result saveColumnsBulk(AccountDto accountDto, ColumnBulkSaveDto request);

    /**
     * 붙여넣은 용어명/영문약어명 목록을 표준용어로 찾는다. (입력 순서대로, 줄마다 결과 하나)
     */
    List<TermResolveDto> resolveTerms(AccountDto accountDto, List<String> inputs);
}
