package com.koboolean.metagen.data.dictionary.controller;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.data.dictionary.service.DataDictionaryService;
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
            @RequestParam(required = false) String sort,
            @Parameter(description = "조회 용어명", example = "용어명1")
            @RequestParam(required = false) String searchQuery,
            @Parameter(description = "조회컬럼 명", example = "id")
            @RequestParam(required = false) String searchColumn
    ) {
        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<StandardTermDto> standardTermDataPage = dataDictionaryService.getStandardTermsData(pageable, accountDto, searchColumn, searchQuery);
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
            @RequestParam(required = false) String sort,
            @Parameter(description = "조회 단어명", example = "단어명1")
            @RequestParam(required = false) String searchQuery,
            @Parameter(description = "조회컬럼 명", example = "id")
            @RequestParam(required = false) String searchColumn
    ) {
        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<StandardWordDto> standardWordsDataPage = dataDictionaryService.getStandardWordsData(pageable, accountDto, searchColumn, searchQuery);
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
            @RequestParam(required = false) String sort,
            @Parameter(description = "조회 도메인명", example = "도메인명1")
            @RequestParam(required = false) String searchQuery,
            @Parameter(description = "조회컬럼 명", example = "id")
            @RequestParam(required = false) String searchColumn
    ) {
        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<StandardDomainDto> standardDomainsDataPage = dataDictionaryService.getStandardDomainsData(pageable, accountDto, searchColumn, searchQuery);
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

    @Operation(summary = "도메인 승인여부 수정", description = "도메인 승인여부를 '승인', '미승인'으로 수정한다.")
    @PatchMapping(value = "/approvalStandardDomains/{isChecked}")
    public ResponseEntity<Map<String, Boolean>> approvalStandardDomains(@RequestBody List<StandardDomainDto> standardDomains
            , @AuthenticationPrincipal AccountDto accountDto
            , @PathVariable boolean isChecked) {

        dataDictionaryService.approvalStandardDomains(standardDomains, accountDto, isChecked);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "용어 승인여부 수정", description = "용어 승인여부를 '승인', '미승인'으로 수정한다.")
    @PatchMapping(value = "/approvalStandardTerms/{isChecked}")
    public ResponseEntity<Map<String, Boolean>> approvalStandardTerms(@RequestBody List<StandardTermDto> standardTerms
            , @AuthenticationPrincipal AccountDto accountDto
            , @PathVariable boolean isChecked) {

        dataDictionaryService.approvalStandardTerms(standardTerms, accountDto, isChecked);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "단어 승인여부 수정", description = "단어 승인여부를 '승인', '미승인'으로 수정한다.")
    @PatchMapping(value = "/approvalStandardWords/{isChecked}")
    public ResponseEntity<Map<String, Boolean>> approvalStandardWords(@RequestBody List<StandardWordDto> standardWords
            , @AuthenticationPrincipal AccountDto accountDto
            , @PathVariable boolean isChecked) {

        dataDictionaryService.approvalStandardWords(standardWords, accountDto, isChecked);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "도메인 삭제", description = "도메인 데이터를 삭제처리한다.")
    @DeleteMapping(value = "/deleteDataDictionary/standardDomains")
    public ResponseEntity<Map<String, Boolean>> deleteDataDictionaryStandardDomains(@RequestBody List<StandardDomainDto> standardDomains
            , @AuthenticationPrincipal AccountDto accountDto) {


        dataDictionaryService.deleteDataDictionaryStandardDomains(standardDomains, accountDto);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "용어 삭제", description = "용어 데이터를 삭제처리한다.")
    @DeleteMapping(value = "/deleteDataDictionary/standardTerms")
    public ResponseEntity<Map<String, Boolean>> deleteDataDictionaryStandardTerms(@RequestBody List<StandardTermDto> standardTerms
            , @AuthenticationPrincipal AccountDto accountDto) {

        dataDictionaryService.deleteDataDictionaryStandardTerms(standardTerms, accountDto);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "단어 삭제", description = "단어 데이터를 삭제처리한다.")
    @DeleteMapping(value = "/deleteDataDictionary/standardWords")
    public ResponseEntity<Map<String, Boolean>> deleteDataDictionaryStandardWords(@RequestBody List<StandardWordDto> standardWords
            , @AuthenticationPrincipal AccountDto accountDto) {

        dataDictionaryService.deleteDataDictionaryStandardWords(standardWords, accountDto);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "단어/도메인 조회", description = "용어 등록을 위한 단어 및 도메인을 조회한다.")
    @GetMapping(value = "/getStandardTerms/popup/{termNm}")
    public ResponseEntity<Map<String, Object>> searchStandardTermList(@PathVariable String termNm, @AuthenticationPrincipal AccountDto accountDto) {
        return ResponseEntity.ok(dataDictionaryService.getStandardTermList(termNm, accountDto));
    }

    @Operation(summary = "용어 등록", description = "표준용어를 등록한다.")
    @PostMapping(value = "/insertDataDictionary/standardTerms")
    public ResponseEntity<Map<String, Boolean>> insertStandardTerms(@AuthenticationPrincipal AccountDto accountDto, @RequestBody StandardTermDto standardTermDto){

        dataDictionaryService.insertStandardTerms(accountDto, standardTermDto);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "용어 수정", description = "표준용어를 수정한다.")
    @PutMapping(value = "/updateDataDictionary/standardTerms")
    public ResponseEntity<Map<String, Boolean>> updateStandardTerms(@AuthenticationPrincipal AccountDto accountDto, @RequestBody StandardTermDto standardTermDto){

        dataDictionaryService.updateStandardTerms(accountDto, standardTermDto);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "도메인 등록", description = "표준 도메인을 등록한다.")
    @PostMapping(value = "/insertDataDictionary/standardDomains")
    public ResponseEntity<Map<String, Boolean>> insertStandardDomains(@AuthenticationPrincipal AccountDto accountDto, @RequestBody StandardDomainDto standardDomainDto){

        dataDictionaryService.insertStandardDomain(accountDto, standardDomainDto);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "도메인 수정", description = "표준 도메인을 수정한다.")
    @PutMapping(value = "/updateDataDictionary/standardDomains")
    public ResponseEntity<Map<String, Boolean>> updateStandardDomains(@AuthenticationPrincipal AccountDto accountDto, @RequestBody StandardDomainDto standardDomainDto){

        dataDictionaryService.updateStandardDomain(accountDto, standardDomainDto);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "단어 등록", description = "표준 단어를 등록한다.")
    @PostMapping(value = "/insertDataDictionary/standardWords")
    public ResponseEntity<Map<String, Boolean>> insertStandardWords(@AuthenticationPrincipal AccountDto accountDto, @RequestBody StandardWordDto standardWordDto){

        dataDictionaryService.insertStandardWord(accountDto, standardWordDto);

        return ResponseEntity.ok(Map.of("result", true));
    }

    @Operation(summary = "단어 수정", description = "표준 단어를 수정한다.")
    @PutMapping(value = "/updateDataDictionary/standardWords")
    public ResponseEntity<Map<String, Boolean>> updateStandardWords(@AuthenticationPrincipal AccountDto accountDto, @RequestBody StandardWordDto standardWordDto){

        dataDictionaryService.updateStandardWords(accountDto, standardWordDto);

        return ResponseEntity.ok(Map.of("result", true));
    }
}
