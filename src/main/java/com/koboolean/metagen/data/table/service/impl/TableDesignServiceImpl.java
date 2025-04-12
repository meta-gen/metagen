package com.koboolean.metagen.data.table.service.impl;

import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.data.table.repository.TableInfoRepository;
import com.koboolean.metagen.data.table.service.TableDesignService;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableDesignServiceImpl implements TableDesignService {

    private final TableInfoRepository tableInfoRepository;

    @Override
    public List<ColumnDto> selectTableInfoColumn() {
        return List.of(
                new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX),
                new ColumnDto("테이블명", "tableName", ColumnType.STRING, RowType.TEXT, true, true),
                new ColumnDto("테이블 설명", "tableDescription", ColumnType.STRING, RowType.TEXT, true, true),
                new ColumnDto("마스터 테이블 여부", "isMasterTable", ColumnType.STRING, RowType.TEXT, false, false),
                new ColumnDto("정렬 순서", "sortOrder", ColumnType.NUMBER, RowType.TEXT, true, false),
                new ColumnDto("승인 여부", "isApproval", ColumnType.STRING, RowType.TEXT, false, false)
        );
    }

    @Override
    public Page<TableInfoDto> selectTableInfoData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery) {
        Long projectId = accountDto.getProjectId();

        return tableInfoRepository.findAllByProjectId(projectId, pageable).map(TableInfoDto::fromEntity);
    }
}
