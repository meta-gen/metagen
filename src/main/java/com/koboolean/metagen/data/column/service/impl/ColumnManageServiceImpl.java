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
import com.koboolean.metagen.utils.ExcelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
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
    private static final int FIRST_ROW = 1;

    @Override
    public List<ColumnDto> selectTableColumn() {
        return List.of(
                new ColumnDto("", "sortOrder", ColumnType.NUMBER, RowType.CHECKBOX),
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
            throw new CustomException(ErrorCode.SAVED_DATA_EXISTS, allByProjectIdAndColumnName.get(0).getColumnName());
        }

        TableInfo tableInfo = tableInfoRepository.findByIdAndProjectId(columnInfoDto.getTableInfoId(), projectId);
        StandardTerm standardTerm = standardTermRepository.findByIdAndProjectId(columnInfoDto.getTermId(), projectId);

        ColumnInfo columnInfo = columnInfoDto.toEntity();
        columnInfo.setTableInfo(tableInfo);
        columnInfo.setProjectId(projectId);

        columnInfo.setStandardTerms(standardTerm);

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
//        if(!AuthUtil.isApprovalAvailable()){
//            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
//        }

        Long projectId = accountDto.getProjectId();

        columnInfoDtos.forEach(columnInfoDto -> {
            ColumnInfo columnInfo = columnInfoRepository.findByProjectIdAndId(projectId, columnInfoDto.getId());

            columnInfo.setIsApproval(Boolean.parseBoolean(type));
        });

    }

    @Override
    @Transactional
    public void deleteColumn(AccountDto accountDto, List<ColumnInfoDto> columnInfoDtos) {
//        if(!AuthUtil.isApprovalAvailable()){
//            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
//        }

        Long projectId = accountDto.getProjectId();

        columnInfoDtos.forEach(columnInfoDto -> {
            ColumnInfo columnInfo = columnInfoRepository.findByProjectIdAndId(projectId, columnInfoDto.getId());

            if(columnInfo.getIsApproval()){
                throw new CustomException(ErrorCode.APPROVED_DATA_CANNOT_BE_DELETED);
            }

            TableInfo tableInfo = tableInfoRepository.findByTableNameAndProjectId(columnInfoDto.getTableName(), projectId);
            StandardTerm standardTerm = standardTermRepository.findByIdAndProjectId(columnInfoDto.getTermId(), projectId);

            columnInfoRepository.delete(columnInfo);

            tableInfo.getColumns().remove(columnInfo);
            standardTerm.getColumnInfos().remove(columnInfo);
        });

    }

    @Override
    @Transactional
    public void uploadColumnExcelFile(MultipartFile file, AccountDto accountDto) throws IOException {
        Long projectId = accountDto.getProjectId();

        // 관리자의 경우 승인으로 저장, 아닐경우 관리자가 승인할 수 있도록 저장
        boolean isApprovalAvailable = AuthUtil.isApprovalAvailable();

        List<String> tableExcelHeaders = List.of(
                "tableName",
                "columnName",
                "columnDesc",
                "dataType",
                "maxLength",
                "precision",
                "scale",
                "isNullable",
                "defaultValue",
                "sortOrder",
                "isMasterData",
                "refTableName",
                "isRequired",
                "isSensitive",
                "isPk",
                "isUnique",
                "isIndex",
                "isEncrypted",
                "example"
        );

        List<Map<String, String>> tableExcelData = ExcelUtils.parseExcelFile(file, 0, tableExcelHeaders, false, FIRST_ROW);

        tableExcelData.forEach(map -> {

            if(map.get("tableName").isEmpty()){
                return;
            }

            // 검증수행
            String[] numberData = {"maxLength", "precision", "scale", "sortOrder"};

            for(int i = 0; i < numberData.length; i++) {
                String trim = map.get(numberData[i]).replaceAll("[^0-9]", "").trim();

                if(trim.isEmpty()){
                    trim = "0";
                }

                map.put(numberData[i], trim);
            }

            String[] booleanData = {"isNullable", "isMasterData", "isRequired", "isSensitive", "isPk", "isUnique", "isIndex", "isEncrypted"};

            for(int i = 0; i < booleanData.length; i++) {
                if(map.get(booleanData[i]).isEmpty()){
                    map.put(booleanData[i], "N");
                }

                if(!map.get(booleanData[i]).equals("Y") && !map.get(booleanData[i]).equals("N")){
                    throw new CustomException(ErrorCode.COLUMN_BOOLEAN_TYPE_DEF);
                }
            }

            String tableName = map.get("tableName").toUpperCase();

            // 테이블 정보 확인 및 테이블 정보 조회
            List<TableInfo> allByTableNameAndProjectId = tableInfoRepository.findAllByTableNameAndProjectId(tableName, projectId);

            if(allByTableNameAndProjectId.isEmpty()){
                throw new CustomException(ErrorCode.TABLE_IS_NOT_DEFINED, tableName);
            }

            TableInfo tableInfo = allByTableNameAndProjectId.get(0);

            // 표준용어 등록여부 확인 및 표준용어 정보 조회
            String columnName = map.get("columnName");
            List<StandardTerm> terms = standardTermRepository.findALlByProjectIdAndCommonStandardTermAbbreviation(projectId, columnName.toUpperCase());

            if(terms.isEmpty()){
                terms = standardTermRepository.findALlByProjectIdAndCommonStandardTermAbbreviation(projectId, columnName);
            }

            if(terms.isEmpty()){
                throw new CustomException(ErrorCode.TERM_IS_NOT_DEFINED, columnName);
            }

            StandardTerm standardTerm = terms.get(0);

            ColumnInfo columnInfo = ColumnInfo.builder()
                    .projectId(projectId)
                    .tableName(tableName)
                    .columnName(columnName)
                    .columnDesc(map.get("columnDesc"))
                    .dataType(map.get("dataType"))
                    .maxLength(Integer.parseInt(map.get("maxLength")))
                    .precision(new BigDecimal(map.get("precision")))
                    .scale(Integer.parseInt(map.get("scale")))
                    .isNullable(map.get("isNullable").equals("Y"))
                    .defaultValue(map.get("defaultValue"))
                    .sortOrder(Integer.parseInt(map.get("sortOrder")))
                    .isMasterData(map.get("isMasterData").equals("Y"))
                    .refTableName(map.get("refTableName"))
                    .isRequired(map.get("isPk").equals("Y") || map.get("isRequired").equals("Y"))
                    .isSensitive(map.get("isSensitive").equals("Y"))
                    .isPk(map.get("isPk").equals("Y"))
                    .isUnique(map.get("isPk").equals("Y") || map.get("isUnique").equals("Y"))
                    .isIndex(map.get("isIndex").equals("Y"))
                    .isEncrypted(map.get("isEncrypted").equals("Y"))
                    .example(map.get("example"))
                    .standardTerms(standardTerm)
                    .tableInfo(tableInfo)
                    .isApproval(isApprovalAvailable)
                    .build();

            columnInfoRepository.save(columnInfo);

            tableInfo.getColumns().add(columnInfo);
            standardTerm.getColumnInfos().add(columnInfo);
        });
    }

    @Override
    public List<ColumnInfoDto> selectColumnDetail(AccountDto accountDto, Long id) {

        Long projectId = accountDto.getProjectId();

        ColumnInfo columnInfo = columnInfoRepository.findByProjectIdAndId(projectId, id);

        TableInfo tableInfo = columnInfo.getTableInfo();

        List<ColumnInfoDto> collect = tableInfo.getColumns().stream().map(ColumnInfoDto::fromEntity).collect(Collectors.toList());

        for(ColumnInfoDto dto : collect){
            if(dto.getIsApproval().equals("Y")){
                return List.of();
            }
        }

        return collect;
    }

    @Override
    @Transactional
    public void updateSortOrder(AccountDto accountDto, List<ColumnInfoDto> columnInfoDtos) {
        Long projectId = accountDto.getProjectId();

        columnInfoDtos.forEach(columnInfoDto -> {
            ColumnInfo columnInfo = columnInfoRepository.findByIdAndProjectId(columnInfoDto.getId(), projectId);

            if(columnInfo.getIsApproval()){
                throw new CustomException(ErrorCode.APPROVED_DATA_CANNOT_BE_UPDATE);
            }

            columnInfo.setSortOrder(columnInfoDto.getSortOrder());
        });
    }
}
