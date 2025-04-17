package com.koboolean.metagen.data.column.controller;

import com.koboolean.metagen.data.column.domain.dto.ColumnInfoDto;
import com.koboolean.metagen.data.column.repository.ColumnInfoRepository;
import com.koboolean.metagen.data.column.service.ColumnManageService;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.utils.PageableUtil;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Column Design API", description = "컬럼 설계 API")
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
    @PostMapping("/updateColumn")
    public ResponseEntity<Map<String, Object>> insertColumn(@AuthenticationPrincipal AccountDto accountDto, @RequestBody ColumnInfoDto columnInfoDto) {
        columnManageService.insertColumn(accountDto, columnInfoDto);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "등록 가능 용어 조회", description = "등록가능 용어를 조회합니다.")
    @GetMapping("/selectColumn/term/{type}/{data}")
    public ResponseEntity<Map<String, Object>> selectTermData(@AuthenticationPrincipal AccountDto accountDto, @PathVariable(value = "type") String type, @PathVariable(value = "data") String data) {

        List<StandardTermDto> termDtos = columnManageService.selectTermData(accountDto, type, data);

        return ResponseEntity.ok(Map.of("result", true, "termDtos", termDtos));
    }

    @Operation(summary = "컬럼 승인여부 승인/미승인 수정", description = "컬럼의 승인여부를 승인/미승인 처리를 진행한다.")
    @PatchMapping("/updateColumn/{type}")
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
}
