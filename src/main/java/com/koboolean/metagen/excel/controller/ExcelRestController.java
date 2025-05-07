package com.koboolean.metagen.excel.controller;

import com.koboolean.metagen.excel.domain.dto.ExcelDto;
import com.koboolean.metagen.excel.service.ExcelService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.utils.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
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
@Tag(name = "Excel API", description = "Excel 관련 API")
public class ExcelRestController {

    private final ExcelService excelService;

    @Operation(
            summary = "Excel 템플릿 다운로드",
            description = "주어진 파일명을 기반으로 Excel 템플릿을 다운로드합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/downloadTemplate/{templateName}")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable(value = "templateName") String templateName) {
        try {
            return excelService.getExcelFile(templateName);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 경로가 잘못되었습니다.", e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽을 수 없습니다.", e);
        }
    }

    @Operation(
            summary = "설계서 다운로드",
            description = "주어진 정보를 기반으로 Excel 설계서를 출력합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/printDesign")
    public ResponseEntity<Resource> downloadDesignTemplate(@RequestBody ExcelDto excelDto, @AuthenticationPrincipal AccountDto accountDto) {
        try {
            return excelService.getDesignExcelFile(excelDto, accountDto);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 경로가 잘못되었습니다.", e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽을 수 없습니다.", e);
        }
    }

    @Operation(
            summary = "시나리오 다운로드",
            description = "주어진 정보를 기반으로 Excel 테스트 시나리오를 출력합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/printTest")
    public ResponseEntity<Resource> downloadTestTemplate(@RequestBody ExcelDto excelDto, @AuthenticationPrincipal AccountDto accountDto) {
        try {
            return excelService.getTestExcelFile(excelDto, accountDto);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 경로가 잘못되었습니다.", e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽을 수 없습니다.", e);
        }
    }

}
