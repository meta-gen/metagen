package com.koboolean.metagen.data.table.service.impl;

import com.koboolean.metagen.data.column.repository.ColumnInfoRepository;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardWordRepository;
import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.data.table.domain.dto.TableNameDto;
import com.koboolean.metagen.data.table.domain.entity.TableInfo;
import com.koboolean.metagen.data.table.repository.TableInfoRepository;
import com.koboolean.metagen.data.table.service.TableDesignService;
import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.common.grid.enums.ColumnType;
import com.koboolean.metagen.common.grid.enums.RowType;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.project.domain.entity.ProjectMember;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableDesignServiceImpl implements TableDesignService {

    private final TableInfoRepository tableInfoRepository;
    private final ColumnInfoRepository columnInfoRepository;
    private final StandardWordRepository standardWordRepository;
    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;
    private static final int FIRST_ROW = 1;

    @Override
    public List<ColumnDto> selectTableInfoColumn() {
        return List.of(
                new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX),
                new ColumnDto("테이블명", "tableName", ColumnType.STRING, RowType.TEXT, true, true),
                new ColumnDto("테이블 한글명", "tableKorName", ColumnType.STRING, RowType.TEXT, true, true),
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
            case "tableKorName" ->
                    tableInfoRepository.findAllByProjectIdAndTableKorNameLike(projectId, searchQuery, pageable);
            default -> null;
        };

        return tableInfoDtos.map(TableInfoDto::fromEntity);
    }

    @Override
    @Transactional
    public void saveTable(TableInfoDto tableInfoDto, AccountDto accountDto) {
        Long projectId = accountDto.getProjectId();

        // 신규 테이블은 한글명(예: 사용_내역_관리)을 표준 단어로 변환한 이름으로만 등록한다.
        if(tableInfoDto.getTableKorName() == null || tableInfoDto.getTableKorName().isBlank()){
            throw new CustomException(ErrorCode.TABLE_KOR_NAME_REQUIRED);
        }
        applyStandardTableName(tableInfoDto, accountDto);

        String tableName = tableInfoDto.getTableName();

        if(!tableInfoRepository.findAllByTableNameAndProjectId(tableName, projectId).isEmpty()){
            throw new CustomException(ErrorCode.SAVED_DATA_EXISTS, tableName);
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
    public TableNameDto generateTableName(String tableKorName, AccountDto accountDto) {
        Long projectId = accountDto.getProjectId();

        // "사용_내역_관리", "사용 내역 관리" 모두 허용
        List<String> inputWords = Arrays.stream((tableKorName == null ? "" : tableKorName).trim().split("[_\\s]+"))
                .filter(word -> !word.isBlank())
                .toList();

        List<TableNameDto.Word> words = inputWords.stream()
                .map(word -> toTableNameWord(word, projectId))
                .toList();

        boolean isValid = !words.isEmpty() && words.stream().allMatch(word -> word.status() == TableNameDto.Status.APPROVED);

        // 프로젝트 설정의 접두어 (예: TB_) - 저장 시 정규화되어 있으므로 그대로 붙인다.
        String tablePrefix = projectRepository.findById(projectId)
                .map(Project::getTablePrefix)
                .filter(prefix -> !prefix.isBlank())
                .orElse(null);

        String tableName = isValid
                ? (tablePrefix == null ? "" : tablePrefix)
                    + words.stream().map(word -> word.abbreviation().toUpperCase()).collect(Collectors.joining("_"))
                : null;

        return new TableNameDto(tableName, String.join(" ", inputWords), tablePrefix, words, isValid);
    }

    /**
     * 단어 하나를 표준 단어로 찾는다. 같은 이름이 여러 건이면 승인된 단어를 우선한다.
     */
    private TableNameDto.Word toTableNameWord(String word, Long projectId) {
        List<StandardWord> standardWords = standardWordRepository.findAllByProjectIdAndCommonStandardWordName(projectId, word);

        Optional<StandardWord> approved = standardWords.stream()
                .filter(standardWord -> Boolean.TRUE.equals(standardWord.getIsApproval()))
                .findFirst();

        if(approved.isPresent()){
            StandardWord standardWord = approved.get();
            return new TableNameDto.Word(word, standardWord.getCommonStandardWordAbbreviation(),
                    standardWord.getCommonStandardWordEnglishName(), TableNameDto.Status.APPROVED);
        }

        if(!standardWords.isEmpty()){
            StandardWord standardWord = standardWords.get(0);
            return new TableNameDto.Word(word, standardWord.getCommonStandardWordAbbreviation(),
                    standardWord.getCommonStandardWordEnglishName(), TableNameDto.Status.NOT_APPROVED);
        }

        return new TableNameDto.Word(word, null, null, TableNameDto.Status.NOT_FOUND);
    }

    /**
     * 한글명을 표준 단어로 변환해 테이블명/한글명에 반영한다. 테이블 설명이 비어있으면 한글명으로 채운다.
     */
    private void applyStandardTableName(TableInfoDto tableInfoDto, AccountDto accountDto) {
        TableNameDto tableNameDto = generateTableName(tableInfoDto.getTableKorName(), accountDto);

        if(!tableNameDto.valid()){
            throw new CustomException(ErrorCode.TABLE_WORD_NOT_STANDARD, String.join(", ", tableNameDto.invalidWords()));
        }

        tableInfoDto.setTableName(tableNameDto.tableName());
        tableInfoDto.setTableKorName(tableNameDto.tableKorName());

        if(tableInfoDto.getTableDescription() == null || tableInfoDto.getTableDescription().isBlank()){
            tableInfoDto.setTableDescription(tableNameDto.tableKorName());
        }
    }

    @Override
    @Transactional
    public void updateTable(TableInfoDto tableInfoDto, AccountDto accountDto) {

        Long projectId = accountDto.getProjectId();

        TableInfo tableInfo = tableInfoRepository.findByIdAndProjectId(tableInfoDto.getId(), projectId);

        // 한글명을 입력(변경)한 경우에만 표준 단어 기준으로 테이블명을 다시 만든다. 비어있으면 기존 테이블명/한글명을 유지한다.
        boolean isKorNameChanged = tableInfoDto.getTableKorName() != null && !tableInfoDto.getTableKorName().isBlank();
        if(isKorNameChanged){
            applyStandardTableName(tableInfoDto, accountDto);
            tableInfo.setTableKorName(tableInfoDto.getTableKorName());
        }

        String tableName = tableInfoDto.getTableName().toUpperCase();

        // 수정 중인 테이블 자신은 중복 대상에서 제외한다 (테이블명을 바꾸지 않고 저장하는 경우)
        boolean isDuplicated = tableInfoRepository.findAllByTableNameAndProjectId(tableName, projectId).stream()
                .anyMatch(other -> !other.getId().equals(tableInfo.getId()));

        if(isDuplicated){
            throw new CustomException(ErrorCode.SAVED_DATA_EXISTS, tableName);
        }

        String oldTableName = tableInfo.getTableName();

        if(!tableName.equals(oldTableName)){
            // 컬럼은 테이블명을 따로 저장하므로 소속 컬럼과 참조 테이블명도 함께 변경한다.
            tableInfo.getColumns().forEach(column -> column.setTableName(tableName));
            columnInfoRepository.findAllByProjectIdAndRefTableName(projectId, oldTableName)
                    .forEach(column -> column.setRefTableName(tableName));
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

        // 파일 안 중복 확인용 (같은 테이블명은 처음 행만 등록)
        Set<String> uploadedTableNames = new HashSet<>();

        // 청크(500행) 단위 : 기존 테이블명은 청크당 IN 조회 한 번, 저장은 JDBC 배치, 저장 후 영속성 컨텍스트 비우기
        ExcelUtils.readExcelRowsInChunks(file, 0, tableExcelHeaders, FIRST_ROW, ExcelUtils.UPLOAD_CHUNK_SIZE, rows -> {

            Set<String> chunkTableNames = rows.stream()
                    .map(map -> map.get("tableName").toUpperCase())
                    .filter(tableName -> !tableName.isEmpty())
                    .collect(Collectors.toSet());

            Set<String> existingTableNames = chunkTableNames.isEmpty() ? Set.of()
                    : tableInfoRepository.findAllByProjectIdAndTableNameIn(projectId, chunkTableNames).stream()
                        .map(TableInfo::getTableName)
                        .collect(Collectors.toSet());

            List<TableInfo> tableInfos = new ArrayList<>();

            for (Map<String, String> map : rows) {
                String s = map.get("sortOrder").replaceAll("[^0-9]", "");

                // 값이 입력되어있지 않으면 0을 입력한다.
                if(s.isEmpty()){
                    s = "0";
                }

                String tableName = map.get("tableName").toUpperCase();

                // 빈 테이블명 / 이미 등록된 테이블 / 파일 안에서 앞에 나온 테이블은 건너뛴다.
                if(tableName.isEmpty() || existingTableNames.contains(tableName) || !uploadedTableNames.add(tableName)){
                    continue;
                }

                tableInfos.add(TableInfo.builder()
                        .projectId(projectId)
                        .tableName(tableName)
                        .tableDescription(map.get("tableDescription"))
                        .isMasterTable(map.get("isMasterTable").equals("Y"))
                        .sortOrder(Integer.parseInt(s))
                        .isApproval(isApprovalAvailable)
                        .build());
            }

            tableInfoRepository.saveAll(tableInfos);
            entityManager.flush();
            entityManager.clear();
        });
    }
}
