package com.koboolean.metagen.data.table.service.impl;

import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.data.table.domain.entity.TableInfo;
import com.koboolean.metagen.data.table.repository.TableInfoRepository;
import com.koboolean.metagen.data.table.service.TableDesignService;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.system.project.domain.entity.ProjectMember;
import com.koboolean.metagen.utils.AuthUtil;
import com.koboolean.metagen.utils.ExcelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TableDesignServiceImpl implements TableDesignService {

    private final TableInfoRepository tableInfoRepository;
    private static final int FIRST_ROW = 1;

    @Override
    public List<ColumnDto> selectTableInfoColumn() {
        return List.of(
                new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX),
                new ColumnDto("테이블명", "tableName", ColumnType.STRING, RowType.TEXT, true, true),
                new ColumnDto("테이블 설명", "tableDescription", ColumnType.STRING, RowType.TEXT, true, true),
                new ColumnDto("마스터 테이블 여부", "isMasterTable", ColumnType.STRING, RowType.TEXT, false, false),
                new ColumnDto("정렬 순서", "sortOrder", ColumnType.NUMBER, RowType.TEXT, false, true),
                new ColumnDto("승인 여부", "isApproval", ColumnType.STRING, RowType.TEXT, false, false)
        );
    }

    @Override
    public Page<TableInfoDto> selectTableInfoData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery) {
        Long projectId = accountDto.getProjectId();

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // 검색어가 없을 경우 전체 조회
            return tableInfoRepository.findAllByProjectId(projectId, pageable).map(TableInfoDto::fromEntity);
        }

        if(!searchQuery.isEmpty()) {
            searchQuery = "%" + searchQuery + "%";
        }

        Page<TableInfo> tableInfoDtos = switch (searchColumn) {
            case "tableName" ->
                    tableInfoRepository.findAllByProjectIdAndTableNameLike(projectId, searchQuery, pageable);
            case "tableDescription" ->
                    tableInfoRepository.findAllByProjectIdAndTableDescriptionLike(projectId, searchQuery, pageable);
            default -> null;
        };

        return tableInfoDtos.map(TableInfoDto::fromEntity);
    }

    @Override
    @Transactional
    public void saveTable(TableInfoDto tableInfoDto, AccountDto accountDto) {
        Long projectId = accountDto.getProjectId();

        List<TableInfo> allByTableNameAndProjectId = tableInfoRepository.findAllByTableNameAndProjectId(tableInfoDto.getTableName(), projectId);

        if(!allByTableNameAndProjectId.isEmpty() && allByTableNameAndProjectId.get(0).getTableName().equals(tableInfoDto.getTableName())){
            throw new CustomException(ErrorCode.SAVED_DATA_EXISTS, tableInfoDto.getTableName());
        }

        TableInfo tableInfo = tableInfoDto.toEntity();
        tableInfo.setProjectId(projectId);

        tableInfo.setIsApproval(true);

        if(!AuthUtil.isApprovalAvailable()){
            tableInfo.setIsApproval(false);
        }

        tableInfoRepository.save(tableInfo);
    }

    @Override
    @Transactional
    public void updateTable(TableInfoDto tableInfoDto, AccountDto accountDto) {

        Long projectId = accountDto.getProjectId();

        TableInfo tableInfo = tableInfoRepository.findByIdAndProjectId(tableInfoDto.getId(), projectId);

        String tableName = tableInfoDto.getTableName().toUpperCase();

        List<TableInfo> allByTableNameAndProjectId = tableInfoRepository.findAllByTableNameAndProjectId(tableName, projectId);

        if(!allByTableNameAndProjectId.isEmpty() && allByTableNameAndProjectId.get(0).getTableName().equals(tableName)){
            throw new CustomException(ErrorCode.SAVED_DATA_EXISTS, tableInfoDto.getTableName());
        }

        tableInfo.setTableName(tableName);
        tableInfo.setTableDescription(tableInfoDto.getTableDescription());
        tableInfo.setIsMasterTable(tableInfoDto.getIsMasterTable().equals("Y"));
        tableInfo.setSortOrder(tableInfoDto.getSortOrder());
    }

    @Override
    @Transactional
    public void updateTableIsApproval(boolean isApproval, AccountDto accountDto, List<TableInfoDto> tableInfoDtos) {
//        if(!AuthUtil.isApprovalAvailable()){
//            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
//        }

        tableInfoDtos.forEach(tableInfoDto -> {
            TableInfo tableInfo = tableInfoRepository.findByIdAndProjectId(tableInfoDto.getId(), accountDto.getProjectId());

            tableInfo.setIsApproval(isApproval);
        });


    }

    @Override
    @Transactional
    public void deleteTable(List<TableInfoDto> tableInfoDtos, AccountDto accountDto) {
        Long projectId = accountDto.getProjectId();

        tableInfoDtos.forEach(tableInfoDto -> {
            TableInfo tableInfo = tableInfoRepository.findByIdAndProjectId(tableInfoDto.getId(), projectId);

            if(!tableInfo.getColumns().isEmpty()){
                // 컬럼이 등록되어있을경우 삭제 불가능
                throw new CustomException(ErrorCode.COLUMN_DEPENDENCY_EXISTS);
            }

            if(tableInfo.getIsApproval()){
                // 승인된 데이터는 삭제가 불가능하다.
                throw new CustomException(ErrorCode.APPROVED_DATA_CANNOT_BE_DELETED);
            }

            tableInfoRepository.delete(tableInfo);
        });
    }

    @Override
    @Transactional
    public void uploadTableExcelFile(MultipartFile file, AccountDto accountDto) throws IOException {
        Long projectId = accountDto.getProjectId();

        // 관리자의 경우 승인으로 저장, 아닐경우 관리자가 승인할 수 있도록 저장
        boolean isApprovalAvailable = AuthUtil.isApprovalAvailable();

        List<String> tableExcelHeaders = List.of(
                "tableName",
                "tableDescription",
                "isMasterTable",
                "sortOrder"
        );

        List<Map<String, String>> tableExcelData = ExcelUtils.parseExcelFile(file, 0, tableExcelHeaders, false, FIRST_ROW);

        tableExcelData.forEach(map -> {

            String s = map.get("sortOrder").replaceAll("[^0-9]", "");

            // 값이 입력되어있지 않으면 0을 입력한다.
            if(s.isEmpty()){
                s = "0";
            }

            String tableName = map.get("tableName").toUpperCase();

            TableInfo tableInfo = TableInfo.builder()
                    .projectId(projectId)
                    .tableName(tableName)
                    .tableDescription(map.get("tableDescription"))
                    .isMasterTable(map.get("isMasterTable").equals("Y"))
                    .sortOrder(Integer.parseInt(s))
                    .isApproval(isApprovalAvailable)
                    .build();

            List<TableInfo> allByTableNameAndProjectId = tableInfoRepository.findAllByTableNameAndProjectId(tableName, projectId);

            if(!tableName.isEmpty() && (allByTableNameAndProjectId == null || allByTableNameAndProjectId.isEmpty())){
                tableInfoRepository.save(tableInfo);
            }

        });
    }
}
