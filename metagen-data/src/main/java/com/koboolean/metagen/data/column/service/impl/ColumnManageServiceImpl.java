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
import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.common.grid.enums.ColumnType;
import com.koboolean.metagen.common.grid.enums.RowType;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.util.AuthUtil;
import com.koboolean.metagen.common.utils.ExcelUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ColumnManageServiceImpl implements ColumnManageService {

    private final ColumnInfoRepository columnInfoRepository;
    private final TableInfoRepository tableInfoRepository;
    private final StandardTermRepository standardTermRepository;
    private final EntityManager entityManager;
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

        TableInfo tableInfo = tableInfoRepository.findByIdAndProjectId(columnInfoDto.getTableInfoId(), projectId);
        StandardTerm standardTerm = standardTermRepository.findByIdAndProjectId(columnInfoDto.getTermId(), projectId);

        // 테이블/표준용어를 선택하지 않았거나 다른 프로젝트의 정보면 등록할 수 없다.
        if(tableInfo == null){
            throw new CustomException(ErrorCode.TABLE_IS_NOT_DEFINED);
        }
        if(standardTerm == null){
            throw new CustomException(ErrorCode.NOT_FOUND_TERM_DATA);
        }

        // 같은 테이블 안에서만 컬럼명 중복을 막는다. (다른 테이블에는 같은 표준 컬럼을 쓸 수 있다)
        List<ColumnInfo> duplicates = columnInfoRepository.findAllByProjectIdAndTableInfo_IdAndColumnName(projectId, tableInfo.getId(), columnInfoDto.getColumnName());

        if(!duplicates.isEmpty()){
            throw new CustomException(ErrorCode.SAVED_DATA_EXISTS, tableInfo.getTableName() + "." + columnInfoDto.getColumnName());
        }

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
    public List<ColumnInfoDto> selectDuplicateColumns(AccountDto accountDto, Long tableInfoId, String columnName) {
        return columnInfoRepository.findAllByProjectIdAndTableInfo_IdAndColumnName(accountDto.getProjectId(), tableInfoId, columnName).stream()
                .map(ColumnInfoDto::fromEntity)
                .toList();
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

            // 테이블명은 변경될 수 있으므로 이름이 아닌 연관관계(table_info_id)로 소속 테이블을 찾는다.
            TableInfo tableInfo = columnInfo.getTableInfo();
            StandardTerm standardTerm = standardTermRepository.findByIdAndProjectId(columnInfoDto.getTermId(), projectId);

            columnInfoRepository.delete(columnInfo);

            if(tableInfo != null){
                tableInfo.getColumns().remove(columnInfo);
            }
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

        // 파일 안 중복 확인용 : "테이블ID|컬럼명"
        Set<String> uploadedColumns = new HashSet<>();

        // 업로드 중에는 테이블/표준용어가 바뀌지 않으므로 조회 결과(ID)를 캐시한다. (없으면 null 로 기록)
        Map<String, Long> tableIdByName = new HashMap<>();
        Map<String, Long> termIdByAbbreviation = new HashMap<>();

        // 청크(500행) 단위 : 테이블/용어/중복은 청크당 IN 조회 한 번씩, 저장은 JDBC 배치, 저장 후 영속성 컨텍스트 비우기
        ExcelUtils.readExcelRowsInChunks(file, 0, tableExcelHeaders, FIRST_ROW, ExcelUtils.UPLOAD_CHUNK_SIZE, rows -> {
            List<Map<String, String>> columnRows = rows.stream().filter(map -> !map.get("tableName").isEmpty()).toList();

            loadColumnUploadReferences(projectId, columnRows, tableIdByName, termIdByAbbreviation);
            Set<String> existingColumns = findExistingColumns(projectId, columnRows, tableIdByName);

            List<ColumnInfo> columnInfos = new ArrayList<>();

            // 행마다 기존과 같은 순서로 검증한다 : 숫자/Y·N 형식 → 테이블 → 표준용어 → 파일 안 중복 → 등록된 컬럼 중복
            for (Map<String, String> map : columnRows) {
                String[] numberData = {"maxLength", "precision", "scale", "sortOrder"};

                for (String key : numberData) {
                    String trim = map.get(key).replaceAll("[^0-9]", "").trim();
                    map.put(key, trim.isEmpty() ? "0" : trim);
                }

                String[] booleanData = {"isNullable", "isMasterData", "isRequired", "isSensitive", "isPk", "isUnique", "isIndex", "isEncrypted"};

                for (String key : booleanData) {
                    if (map.get(key).isEmpty()) {
                        map.put(key, "N");
                    }

                    if (!map.get(key).equals("Y") && !map.get(key).equals("N")) {
                        throw new CustomException(ErrorCode.COLUMN_BOOLEAN_TYPE_DEF);
                    }
                }

                String tableName = map.get("tableName").toUpperCase();
                Long tableInfoId = tableIdByName.get(tableName);

                if (tableInfoId == null) {
                    throw new CustomException(ErrorCode.TABLE_IS_NOT_DEFINED, tableName);
                }

                // 표준용어 : 영문약어(대문자) 우선, 없으면 입력값 그대로
                String columnName = map.get("columnName");
                Long termId = termIdByAbbreviation.get(columnName.toUpperCase());
                if (termId == null) {
                    termId = termIdByAbbreviation.get(columnName);
                }

                if (termId == null) {
                    throw new CustomException(ErrorCode.TERM_IS_NOT_DEFINED, columnName);
                }

                // 같은 테이블 안에서만 컬럼명 중복을 막는다. (화면 등록과 같은 기준) - 하나라도 있으면 업로드 전체 취소
                String columnKey = tableInfoId + "|" + columnName;

                if (!uploadedColumns.add(columnKey)) {
                    throw new CustomException(ErrorCode.COLUMN_DUPLICATE_IN_FILE, tableName + "." + columnName);
                }

                if (existingColumns.contains(columnKey)) {
                    throw new CustomException(ErrorCode.SAVED_DATA_EXISTS, tableName + "." + columnName);
                }

                // 연관은 조회 없이 ID 참조로 연결한다. (테이블/용어의 컬럼 목록 컬렉션은 건드리지 않는다 - FK 가 기준)
                columnInfos.add(ColumnInfo.builder()
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
                        .standardTerms(entityManager.getReference(StandardTerm.class, termId))
                        .tableInfo(entityManager.getReference(TableInfo.class, tableInfoId))
                        .isApproval(isApprovalAvailable)
                        .build());
            }

            columnInfoRepository.saveAll(columnInfos);
            entityManager.flush();
            entityManager.clear();
        });
    }

    /**
     * 청크에서 쓰는 테이블명/표준용어(영문약어) 중 캐시에 없는 것을 IN 조회 한 번씩으로 불러온다.
     */
    private void loadColumnUploadReferences(Long projectId, List<Map<String, String>> columnRows,
                                            Map<String, Long> tableIdByName, Map<String, Long> termIdByAbbreviation) {
        Set<String> tableNames = columnRows.stream()
                .map(map -> map.get("tableName").toUpperCase())
                .filter(name -> !tableIdByName.containsKey(name))
                .collect(Collectors.toSet());

        if (!tableNames.isEmpty()) {
            tableNames.forEach(name -> tableIdByName.put(name, null));
            tableInfoRepository.findAllByProjectIdAndTableNameIn(projectId, tableNames)
                    .forEach(table -> {
                        if (tableIdByName.get(table.getTableName()) == null) tableIdByName.put(table.getTableName(), table.getId());
                    });
        }

        Set<String> abbreviations = columnRows.stream()
                .flatMap(map -> Stream.of(map.get("columnName").toUpperCase(), map.get("columnName")))
                .filter(abbreviation -> !termIdByAbbreviation.containsKey(abbreviation))
                .collect(Collectors.toSet());

        if (!abbreviations.isEmpty()) {
            abbreviations.forEach(abbreviation -> termIdByAbbreviation.put(abbreviation, null));
            standardTermRepository.findAllByProjectIdAndCommonStandardTermAbbreviationIn(projectId, abbreviations)
                    .forEach(term -> {
                        if (termIdByAbbreviation.get(term.getCommonStandardTermAbbreviation()) == null) {
                            termIdByAbbreviation.put(term.getCommonStandardTermAbbreviation(), term.getId());
                        }
                    });
        }
    }

    /**
     * 청크의 (테이블, 컬럼명) 중 이미 등록된 조합 → "테이블ID|컬럼명"
     */
    private Set<String> findExistingColumns(Long projectId, List<Map<String, String>> columnRows, Map<String, Long> tableIdByName) {
        Set<Long> tableIds = columnRows.stream()
                .map(map -> tableIdByName.get(map.get("tableName").toUpperCase()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> columnNames = columnRows.stream().map(map -> map.get("columnName")).collect(Collectors.toSet());

        if (tableIds.isEmpty() || columnNames.isEmpty()) {
            return Set.of();
        }

        return columnInfoRepository.findTableColumnKeys(projectId, tableIds, columnNames).stream()
                .map(key -> key[0] + "|" + key[1])
                .collect(Collectors.toSet());
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
