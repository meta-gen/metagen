package com.koboolean.metagen.data.column.service.impl;

import com.koboolean.metagen.data.column.service.ColumnManageService;
import com.koboolean.metagen.data.column.domain.dto.ColumnInfoDto;
import com.koboolean.metagen.data.column.repository.ColumnInfoRepository;
import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.data.table.domain.entity.TableInfo;
import com.koboolean.metagen.data.table.repository.TableInfoRepository;
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
public class ColumnManageServiceImpl implements ColumnManageService {

    private final ColumnInfoRepository columnInfoRepository;
    private final TableInfoRepository tableInfoRepository;

    @Override
    public List<ColumnDto> selectTableColumn() {
        return List.of(
                new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX),
                new ColumnDto("테이블명", "tableName", ColumnType.STRING, RowType.TEXT, true, true),
                new ColumnDto("컬럼명", "columnName", ColumnType.STRING, RowType.TEXT, true, true),
                new ColumnDto("컬럼 설명", "columnDesc", ColumnType.STRING, RowType.TEXT, true, false),
                new ColumnDto("데이터 타입", "dataType", ColumnType.STRING, RowType.TEXT, true, false),
                new ColumnDto("최대 길이", "maxLength", ColumnType.NUMBER, RowType.TEXT, false,  false),
                new ColumnDto("정밀도", "precision", ColumnType.NUMBER, RowType.TEXT, false,  false),
                new ColumnDto("소수 자릿수", "scale", ColumnType.NUMBER, RowType.TEXT, false,  false),
                new ColumnDto("기본값", "defaultValue", ColumnType.STRING, RowType.TEXT, false,  false),
                new ColumnDto("정렬 순서", "sortOrder", ColumnType.NUMBER, RowType.TEXT, false,  false),
                new ColumnDto("참조 테이블명", "refTableName", ColumnType.STRING, RowType.TEXT, false,  false),
                new ColumnDto("데이터 예시", "example", ColumnType.STRING, RowType.TEXT, false,  false),
                new ColumnDto("NULL 허용 여부", "isNullable", ColumnType.STRING, RowType.TEXT, false,  false),
                new ColumnDto("마스터 데이터 여부", "isMasterData", ColumnType.STRING, RowType.TEXT, false,  false),
                new ColumnDto("필수 입력 여부", "isRequired", ColumnType.STRING, RowType.TEXT, false,  false),
                new ColumnDto("민감정보 여부", "isSensitive", ColumnType.STRING, RowType.TEXT, false,  false),
                new ColumnDto("고유값 여부", "isUnique", ColumnType.STRING, RowType.TEXT, false,  false),
                new ColumnDto("인덱스 생성 여부", "isIndex", ColumnType.STRING, RowType.TEXT, false,  false),
                new ColumnDto("암호화 필요 여부", "isEncrypted", ColumnType.STRING, RowType.TEXT, false,  false),
                new ColumnDto("승인여부", "isApproval", ColumnType.STRING, RowType.TEXT, false,  false)
        );
    }

    @Override
    public Page<ColumnInfoDto> selectTableData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery, Long tableId) {
        Long projectId = accountDto.getProjectId();

        return columnInfoRepository.findByTableInfo_IdAndProjectId(tableId, projectId, pageable).map(ColumnInfoDto::fromEntity);
    }

    @Override
    public List<TableInfoDto> selectTable(AccountDto accountDto, String tableName) {

        Long projectId = accountDto.getProjectId();

        String searchTableName = null;

        if(!tableName.isEmpty()){
            searchTableName = "%" + tableName.toUpperCase() + "%";
        }

        List<TableInfo> tableInfos = tableInfoRepository.findAllByProjectIdAndTableNameLikeAndIsApproval(projectId, searchTableName, true);

        return tableInfos.stream().map(TableInfoDto::fromEntity).toList();
    }
}
