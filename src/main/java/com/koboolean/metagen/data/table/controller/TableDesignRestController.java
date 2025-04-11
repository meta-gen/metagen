package com.koboolean.metagen.data.table.controller;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.table.domain.dto.TableDesignDto;
import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.data.table.domain.entity.TableDesign;
import com.koboolean.metagen.data.table.service.TableDesignService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Table Design API", description = "테이블 설계 API")
public class TableDesignRestController {

    private final TableDesignService tableDesignService;

    @Operation(summary = "테이블 설계 컬럼 조회", description = "테이블 설계 테이블의 컬럼 목록을 조회합니다.")
    @GetMapping("/selectTable/column")
    public ResponseEntity<List<ColumnDto>> selectTableInfoColumn() {
        return ResponseEntity.ok(tableDesignService.selectTableInfoColumn());
    }

    @Operation(summary = "테이블 설계 데이터 조회", description = "테이블 설계 데이터를 조회합니다.")
    @GetMapping("/selectTable/data")
    public ResponseEntity<Map<String,Object>> selectTableInfoData(
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
            @RequestParam(required = false, value = "searchColumn") String searchColumn
    ) {
        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<TableInfoDto> tableDesigns = tableDesignService.selectTableInfoData(pageable, accountDto, searchColumn, searchQuery);
        return PageableUtil.getGridPageableMap(tableDesigns);
    }

    @Operation(summary = "테이블 내 컬럼 조회", description = "테이블 내 컬럼 목록을 조회합니다.")
    @GetMapping("/selectColumn/column")
    public ResponseEntity<List<ColumnDto>> selectTableColumn() {
        return ResponseEntity.ok(tableDesignService.selectTableColumn());
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
        Page<TableDesignDto> tableDesigns = tableDesignService.selectTableData(pageable, accountDto, searchColumn, searchQuery, tableId);
        return PageableUtil.getGridPageableMap(tableDesigns);
    }


}
