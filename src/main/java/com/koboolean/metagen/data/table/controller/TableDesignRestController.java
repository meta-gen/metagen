package com.koboolean.metagen.data.table.controller;

import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
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

    @Operation(summary = "테이블 관리 테이블 등록", description = "테이블 관리 테이블 데이터를 등록합니다.")
    @PostMapping("/saveTable")
    public ResponseEntity<Map<String, Boolean>> saveTable(@RequestBody TableInfoDto tableInfoDto, @AuthenticationPrincipal AccountDto accountDto) {
        tableDesignService.saveTable(tableInfoDto, accountDto);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "테이블 관리 테이블 수정", description = "테이블 관리 테이블 데이터를 수정합니다.")
    @PutMapping("/saveTable")
    public ResponseEntity<Map<String, Boolean>> updateTable(@RequestBody TableInfoDto tableInfoDto, @AuthenticationPrincipal AccountDto accountDto) {
        tableDesignService.updateTable(tableInfoDto, accountDto);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "테이블 관리 테이블 승인/승인취소", description = "테이블 관리 테이블 데이터를  승인/승인취소합니다.")
    @PatchMapping("/saveTable/{isApproval}")
    public ResponseEntity<Map<String, Boolean>> updateTableIsApproval(@PathVariable(value = "isApproval") boolean isApproval, @AuthenticationPrincipal AccountDto accountDto, @RequestBody List<TableInfoDto> tableInfoDtos) {
        tableDesignService.updateTableIsApproval(isApproval, accountDto, tableInfoDtos);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "테이블 관리 테이블 삭제", description = "테이블 관리 테이블 데이터를  삭제합니다.")
    @DeleteMapping("/deleteTable")
    public ResponseEntity<Map<String, Boolean>> deleteTable(@AuthenticationPrincipal AccountDto accountDto, @RequestBody List<TableInfoDto> tableInfoDtos) {
        tableDesignService.deleteTable(tableInfoDtos, accountDto);
        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "테이블 템플릿 엑셀 업로드", description = "테이블 템플릿 엑셀을 파싱하여 테이블에 저장한다.")
    @PostMapping(value = "/uploadTableExcelFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadTableExcelFile(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal AccountDto accountDto) throws IOException {

        try {
            tableDesignService.uploadTableExcelFile(file, accountDto);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽을 수 없습니다.", e);
        }

        return ResponseEntity.ok(Map.of("result", "success"));
    }


}
