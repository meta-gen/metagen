package com.koboolean.metagen.data.column.service.impl;

import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import com.koboolean.metagen.data.column.service.ColumnManageService;
import com.koboolean.metagen.data.column.domain.dto.ColumnInfoDto;
import com.koboolean.metagen.data.column.repository.ColumnInfoRepository;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.data.table.domain.entity.TableInfo;
import com.koboolean.metagen.data.table.repository.TableInfoRepository;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColumnManageServiceImpl implements ColumnManageService {

    private final ColumnInfoRepository columnInfoRepository;
    private final TableInfoRepository tableInfoRepository;
    private final StandardTermRepository standardTermRepository;

    @Override
    public List<ColumnDto> selectTableColumn() {
        return List.of(
                new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX),
                new ColumnDto("테이블명", "tableName", ColumnType.STRING, RowType.TEXT, true, true),
                new ColumnDto("컬럼명", "columnName", ColumnType.STRING, RowType.TEXT, true, true),
                new ColumnDto("컬럼 설명", "columnDesc", ColumnType.STRING, RowType.TEXT, true, false),
                new ColumnDto("PK 여부", "isPk", ColumnType.STRING, RowType.TEXT, false,  false),
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

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // 검색어가 없을 경우 전체 조회
            return columnInfoRepository.findByProjectId(projectId, pageable).map(ColumnInfoDto::fromEntity);
        }

        if(!searchQuery.isEmpty()) {
            searchQuery = "%" + searchQuery + "%";
        }

        Page<ColumnInfo> columnInfos = switch (searchColumn) {
            case "tableName" ->
                    columnInfoRepository.findAllByProjectIdAndTableNameLike(projectId, searchQuery.toUpperCase(), pageable);
            case "columnName" ->
                    columnInfoRepository.findAllByProjectIdAndColumnNameLike(projectId, searchQuery, pageable);
            case "columnDesc" ->
                    columnInfoRepository.findAllByProjectIdAndColumnDescLike(projectId, searchQuery, pageable);
            case "dataType" ->
                    columnInfoRepository.findAllByProjectIdAndDataTypeLike(projectId, searchQuery, pageable);
            default -> null;
        };

        return columnInfos.map(ColumnInfoDto::fromEntity);
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

    @Override
    public List<ColumnInfoDto> selectTableColumn(AccountDto accountDto, Long tableId) {
        TableInfo tableInfo = tableInfoRepository.findALlByProjectIdAndId(accountDto.getProjectId(), tableId);
        return tableInfo.getColumns().stream().map(ColumnInfoDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void insertColumn(AccountDto accountDto, ColumnInfoDto columnInfoDto) {
        Long projectId = accountDto.getProjectId();

        List<ColumnInfo> allByProjectIdAndColumnName = columnInfoRepository.findAllByProjectIdAndColumnName(projectId, columnInfoDto.getColumnName());

        if(!allByProjectIdAndColumnName.isEmpty()){
            throw new CustomException(ErrorCode.SAVED_DATA_EXISTS);
        }

        TableInfo tableInfo = tableInfoRepository.findByIdAndProjectId(columnInfoDto.getTableInfoId(), projectId);
        StandardTerm standardTerm = standardTermRepository.findByIdAndProjectId(columnInfoDto.getTermId(), projectId);

        ColumnInfo columnInfo = columnInfoDto.toEntity();
        columnInfo.setTableInfo(tableInfo);
        columnInfo.setProjectId(projectId);

        if (columnInfo.getStandardTerms() == null) {
            columnInfo.setStandardTerms(List.of(standardTerm));
        }else{
            columnInfo.getStandardTerms().add(standardTerm);
        }

        columnInfo.setIsApproval(true);

        if(!AuthUtil.isApprovalAvailable()){
            columnInfo.setIsApproval(false);
        }

        columnInfoRepository.save(columnInfo);
        tableInfo.getColumns().add(columnInfo);
        standardTerm.getColumnInfos().add(columnInfo);
    }

    @Override
    public List<StandardTermDto> selectTermData(AccountDto accountDto, String type, String data) {

        List<StandardTerm> terms = switch(type){
            case "termName" -> standardTermRepository.findAllByProjectIdAndCommonStandardTermNameContaining(accountDto.getProjectId(), data);
            case "termAbbreviation" -> standardTermRepository.findAllByProjectIdAndCommonStandardTermAbbreviationContaining(accountDto.getProjectId(), data);
            default -> null;
        };

        return terms.stream().map(StandardTermDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateColumnApproval(AccountDto accountDto, List<ColumnInfoDto> columnInfoDtos, String type) {
        if(!AuthUtil.isApprovalAvailable()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        Long projectId = accountDto.getProjectId();

        columnInfoDtos.forEach(columnInfoDto -> {
            ColumnInfo columnInfo = columnInfoRepository.findByProjectIdAndId(projectId, columnInfoDto.getId());

            columnInfo.setIsApproval(Boolean.parseBoolean(type));
        });

    }

    @Override
    @Transactional
    public void deleteColumn(AccountDto accountDto, List<ColumnInfoDto> columnInfoDtos) {
        if(!AuthUtil.isApprovalAvailable()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        Long projectId = accountDto.getProjectId();

        columnInfoDtos.forEach(columnInfoDto -> {
            ColumnInfo columnInfo = columnInfoRepository.findByProjectIdAndId(projectId, columnInfoDto.getId());

            if(columnInfo.getIsApproval()){
                throw new CustomException(ErrorCode.APPROVED_DATA_CANNOT_BE_DELETED);
            }

            columnInfoRepository.delete(columnInfo);
        });

    }
}
