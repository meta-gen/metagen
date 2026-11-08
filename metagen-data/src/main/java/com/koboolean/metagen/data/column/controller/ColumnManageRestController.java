package com.koboolean.metagen.data.column.controller;

import com.koboolean.metagen.data.column.domain.dto.ColumnBulkSaveDto;
import com.koboolean.metagen.data.column.domain.dto.ColumnInfoDto;
import com.koboolean.metagen.data.column.repository.ColumnInfoRepository;
import com.koboolean.metagen.data.column.service.ColumnManageService;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.common.utils.PageableUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Column Design API", description = "컬럼 설계 API")
@PreAuthorize("hasRole('USER')")
public class ColumnManageRestController {

    private final ColumnManageService columnManageService;

    @Operation(summary = "테이블 내 컬럼 조회", description = "테이블 내 컬럼 목록을 조회합니다.")
    @GetMapping("/selectColumn/column")
    public ResponseEntity<List<ColumnDto>> selectTableColumn() {
        return ResponseEntity.ok(columnManageService.selectTableColumn());
    }

    @Operation(summary = "테이블 내 데이터 조회", description = "테이블 내 데이터를 조회합니다.")
    @GetMapping("/selectColumn/data")
    public ResponseEntity<Map<String,Object>> selectTableColumnData(
            @Parameter(description = "사용자 인증 정보", hidden = true)
            @AuthenticationPrincipal AccountDto accountDto,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(value = "page") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(value = "size") int size,
            @Parameter(description = "정렬 조건 (예: timestamp,desc;id,asc)", example = "id,desc")
            @RequestParam(required = false, value = "sort") String sort,
            @Parameter(description = "조회 용어명", example = "용어명1")
            @RequestParam(required = false, value = "searchQuery") String searchQuery,
            @Parameter(description = "조회컬럼 명", example = "id")
            @RequestParam(required = false, value = "searchColumn") String searchColumn,
            @Parameter(description = "테이블 ID", example = "0")
            @RequestParam(required = false, value = "tableId") Long tableId
    ) {
        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<ColumnInfoDto> tableDesigns = columnManageService.selectTableData(pageable, accountDto, searchColumn, searchQuery, tableId);
        return PageableUtil.getGridPageableMap(tableDesigns);
    }

    @Operation(summary = "테이블 조회", description = "컬럼 등록을 위한 테이블을 조회합니다.")
    @GetMapping("/selectColumn/table/{tableName}")
    public ResponseEntity<Map<String, Object>> selectTable(@AuthenticationPrincipal AccountDto accountDto
            , @PathVariable(value = "tableName") String tableName) {

        List<TableInfoDto> tableInfoDtos = columnManageService.selectTable(accountDto, tableName);

        return ResponseEntity.ok(Map.of("result", true, "tableInfos", tableInfoDtos));
    }

    @Operation(summary = "테이블 내 컬럼 조회", description = "테이블 조회 팝업 내 테이블 내 컬럼 목록을 조회합니다.")
    @GetMapping("/selectColumn/tableColumn/{tableId}")
    public ResponseEntity<Map<String, Object>> selectTableColumn(@AuthenticationPrincipal AccountDto accountDto, @PathVariable(value = "tableId") Long tableId) {

        List<ColumnInfoDto> columnInfoDtos = columnManageService.selectTableColumn(accountDto, tableId);

        return ResponseEntity.ok(Map.of("result", true, "columnInfos", columnInfoDtos));
    }

    @Operation(summary = "테이블 내 컬럼 등록", description = "테이블 조회 팝업 내 테이블 내 컬럼을 등록합니다.")
    @PostMapping("/saveColumn")
    public ResponseEntity<Map<String, Object>> insertColumn(@AuthenticationPrincipal AccountDto accountDto, @RequestBody ColumnInfoDto columnInfoDto) {
        columnManageService.insertColumn(accountDto, columnInfoDto);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "컬럼명 중복 확인", description = "같은 테이블에 같은 컬럼명이 이미 등록되어 있는지 확인합니다. (등록 시 중복 기준과 동일)")
    @GetMapping("/selectColumn/duplicate")
    public ResponseEntity<Map<String, Object>> selectDuplicateColumns(@AuthenticationPrincipal AccountDto accountDto,
            @RequestParam(value = "tableInfoId") Long tableInfoId,
            @RequestParam(value = "columnName") String columnName) {
        List<ColumnInfoDto> duplicates = columnManageService.selectDuplicateColumns(accountDto, tableInfoId, columnName);

        return ResponseEntity.ok(Map.of("result", true, "duplicated", !duplicates.isEmpty(), "columns", duplicates));
    }

    @Operation(summary = "등록 가능 용어 조회", description = "등록가능 용어를 조회합니다.")
    @GetMapping("/selectColumn/term/{type}/{data}")
    public ResponseEntity<Map<String, Object>> selectTermData(@AuthenticationPrincipal AccountDto accountDto, @PathVariable(value = "type") String type, @PathVariable(value = "data") String data) {

        List<StandardTermDto> termDtos = columnManageService.selectTermData(accountDto, type, data);

        return ResponseEntity.ok(Map.of("result", true, "termDtos", termDtos));
    }

    @Operation(summary = "컬럼 승인여부 승인/미승인 수정", description = "컬럼의 승인여부를 승인/미승인 처리를 진행한다.")
    @PatchMapping("/saveColumn/approval/{type}")
    public ResponseEntity<Map<String, Object>> updateColumnApproval(@AuthenticationPrincipal AccountDto accountDto, @PathVariable(value = "type") String type, @RequestBody List<ColumnInfoDto> columnInfoDtos) {
        columnManageService.updateColumnApproval(accountDto, columnInfoDtos, type);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "컬럼 삭제", description = "등록된 컬럼정보를 삭제한다.")
    @DeleteMapping("/deleteColumn")
    public ResponseEntity<Map<String, Object>> deleteColumn(@AuthenticationPrincipal AccountDto accountDto, @RequestBody List<ColumnInfoDto> columnInfoDtos) {
        columnManageService.deleteColumn(accountDto, columnInfoDtos);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "컬럼 템플릿 엑셀 업로드", description = "컬럼 템플릿 엑셀을 파싱하여 테이블에 저장한다.")
    @PostMapping(value = "/uploadColumnExcelFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadTableExcelFile(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal AccountDto accountDto) throws IOException {

        try {
            columnManageService.uploadColumnExcelFile(file, accountDto);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽을 수 없습니다.", e);
        }

        return ResponseEntity.ok(Map.of("result", "success"));
    }

    @Operation(summary = "컬럼이 속하는 테이블 컬럼내역 조회", description = "컬럼 그리드 선택 시 테이블 컬럼내역 조회")
    @GetMapping(value = "/selectColumn/detail/{id}")
    public ResponseEntity<Map<String, Object>> selectColumnDetail(@PathVariable(value = "id") Long id, @AuthenticationPrincipal AccountDto accountDto) {
        List<ColumnInfoDto> columnInfoDtos = columnManageService.selectColumnDetail(accountDto, id);

        return ResponseEntity.ok(Map.of("result", true, "columnInfos", columnInfoDtos));
    }

    @Operation(summary = "컬럼 정렬순서 변경", description = "컬럼 목록의 정렬순서를 변경한다.")
    @PatchMapping(value = "/saveColumn/sortOrder")
    public ResponseEntity<Map<String, Boolean>> updateSortOrder(@RequestBody List<ColumnInfoDto> columnInfoDtos, @AuthenticationPrincipal AccountDto accountDto) {
        columnManageService.updateSortOrder(accountDto, columnInfoDtos);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "컬럼 일괄 저장", description = "테이블의 컬럼을 한 번에 추가/수정/삭제하고 화면 순서대로 정렬 순서를 저장합니다. 검증 오류가 있으면 아무것도 저장하지 않고 행별 오류를 돌려줍니다.")
    @PutMapping(value = "/saveColumn/bulk")
    public ResponseEntity<ColumnBulkSaveDto.Result> saveColumnsBulk(@RequestBody ColumnBulkSaveDto request, @AuthenticationPrincipal AccountDto accountDto) {
        return ResponseEntity.ok(columnManageService.saveColumnsBulk(accountDto, request));
    }

    @Operation(summary = "표준용어 일괄 찾기", description = "붙여넣은 용어명/영문약어명 목록을 표준용어로 찾습니다. 줄마다 MATCHED / AMBIGUOUS / NOT_FOUND 와 찾은 용어를 돌려줍니다. (최대 300줄)")
    @PostMapping(value = "/selectColumn/term/resolve")
    public ResponseEntity<Map<String, Object>> resolveTerms(@RequestBody List<String> inputs, @AuthenticationPrincipal AccountDto accountDto) {
        return ResponseEntity.ok(Map.of("result", true, "items", columnManageService.resolveTerms(accountDto, inputs)));
    }
}
