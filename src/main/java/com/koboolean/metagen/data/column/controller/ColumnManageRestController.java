package com.koboolean.metagen.data.column.controller;

import com.koboolean.metagen.data.column.domain.dto.ColumnInfoDto;
import com.koboolean.metagen.data.column.repository.ColumnInfoRepository;
import com.koboolean.metagen.data.column.service.ColumnManageService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/selectColumn/table/{tableName}")
    public ResponseEntity<Map<String, Object>> selectTable(@AuthenticationPrincipal AccountDto accountDto
            , @PathVariable(value = "tableName") String tableName) {

        List<TableInfoDto> tableInfoDtos = columnManageService.selectTable(accountDto, tableName);

        return ResponseEntity.ok(Map.of("result", true, "tableInfos", tableInfoDtos));
    }
}
