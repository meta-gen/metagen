package com.koboolean.metagen.data.dictionary.controller;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.data.dictionary.service.DataDictionaryService;
import com.koboolean.metagen.excel.service.ExcelService;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.logs.domain.dto.LogsDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.utils.ExcelUtils;
import com.koboolean.metagen.utils.PageableUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Data Dictionary API", description = "데이터 사전 관리 API")
public class DataDictionaryRestController {

    private final DataDictionaryService dataDictionaryService;

    @Operation(summary = "표준 용어 컬럼 조회", description = "표준 용어 테이블의 컬럼 목록을 조회합니다.")
    @GetMapping("/getStandardTerms/column")
    public ResponseEntity<List<ColumnDto>> getStandardTermsColumn() {
        return ResponseEntity.ok(dataDictionaryService.getStandardTermsColumn());
    }

    @Operation(summary = "표준 용어 데이터 조회", description = "표준 용어 데이터를 조회합니다.")
    @GetMapping("/getStandardTerms/data")
    public ResponseEntity<Map<String,Object>> getStandardTermsData(
            @Parameter(description = "사용자 인증 정보", hidden = true)
            @AuthenticationPrincipal AccountDto accountDto,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam int size,
            @Parameter(description = "정렬 조건 (예: timestamp,desc;id,asc)", example = "id,desc")
            @RequestParam(required = false) String sort
    ) {
        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<StandardTermDto> standardTermDataPage = dataDictionaryService.getStandardTermsData(pageable, accountDto);
        return PageableUtil.getGridPageableMap(standardTermDataPage);
    }

    @Operation(summary = "표준 단어 컬럼 조회", description = "표준 단어 테이블의 컬럼 목록을 조회합니다.")
    @GetMapping("/getStandardWords/column")
    public ResponseEntity<List<ColumnDto>> getStandardWordsColumn() {
        return ResponseEntity.ok(dataDictionaryService.getStandardWordsColumn());
    }

    @Operation(summary = "표준 단어 데이터 조회", description = "표준 단어 데이터를 조회합니다.")
    @GetMapping("/getStandardWords/data")
    public ResponseEntity<Map<String,Object>> getStandardWordsData(
            @Parameter(description = "사용자 인증 정보", hidden = true)
            @AuthenticationPrincipal AccountDto accountDto,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam int size,
            @Parameter(description = "정렬 조건 (예: timestamp,desc;id,asc)", example = "id,desc")
            @RequestParam(required = false) String sort
    ) {
        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<StandardWordDto> standardWordsDataPage = dataDictionaryService.getStandardWordsData(pageable, accountDto);
        return PageableUtil.getGridPageableMap(standardWordsDataPage);
    }

    @Operation(summary = "표준 도메인 컬럼 조회", description = "표준 도메인 테이블의 컬럼 목록을 조회합니다.")
    @GetMapping("/getStandardDomains/column")
    public ResponseEntity<List<ColumnDto>> getStandardDomainsColumn() {
        return ResponseEntity.ok(dataDictionaryService.getStandardDomainsColumn());
    }

    @Operation(summary = "표준 도메인 데이터 조회", description = "표준 도메인 데이터를 조회합니다.")
    @GetMapping("/getStandardDomains/data")
    public ResponseEntity<Map<String,Object>> getStandardDomainsData(
            @Parameter(description = "사용자 인증 정보", hidden = true)
            @AuthenticationPrincipal AccountDto accountDto,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam int size,
            @Parameter(description = "정렬 조건 (예: timestamp,desc;id,asc)", example = "id,desc")
            @RequestParam(required = false) String sort
    ) {
        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<StandardDomainDto> standardDomainsDataPage = dataDictionaryService.getStandardDomainsData(pageable, accountDto);
        return PageableUtil.getGridPageableMap(standardDomainsDataPage);
    }

    @Operation(summary = "데이터사전 엑셀 업로드", description = "업로드한 엑셀 데이터를 파싱하여 데이터사전 테이블에 저장한다.")
    @PostMapping(value = "/uploadDataDictionaryExcelFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadDataDictionaryExcelFile(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal AccountDto accountDto) throws IOException {

        try {
            dataDictionaryService.uploadDictionaryExcelFile(file, accountDto);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽을 수 없습니다.", e);
        }

        return ResponseEntity.ok(Map.of("result", "success"));
    }

}
