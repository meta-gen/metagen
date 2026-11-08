package com.koboolean.metagen.design.excel.util;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.design.code.domain.dto.CodeRuleDetailDto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 템플릿 기반 엑셀 출력 (SXSSF 스트리밍 쓰기)
 * 템플릿(static/excel)이 metagen-app 리소스에 있어 app 모듈에서 검증한다.
 * (검증용으로 결과 파일을 XSSFWorkbook 으로 여는 것은 테스트에서만 허용)
 */
class ExcelExportUtilsTest {

    private final List<File> outputs = new ArrayList<>();

    @AfterEach
    void cleanUp() throws IOException {
        for (File file : outputs) Files.deleteIfExists(file.toPath());
    }

    private Workbook open(File file) throws IOException {
        outputs.add(file);
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return new XSSFWorkbook(in);
        }
    }

    @ParameterizedTest(name = "{0}.xlsx")
    @ValueSource(strings = {"template", "functionSpec", "interfaceSpec", "technicalSpec", "unitTest"})
    @DisplayName("출력용 템플릿은 헤더(1행)만 가진다 : SXSSF 는 템플릿에 이미 있는 행 위치에 쓸 수 없다")
    void templatesHaveHeaderOnly(String templateName) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("static/excel/" + templateName + ".xlsx");
             Workbook workbook = new XSSFWorkbook(in)) {
            for (Sheet sheet : workbook) {
                assertThat(sheet.getLastRowNum()).as(templateName + " / " + sheet.getSheetName()).isZero();
                assertThat(sheet.getRow(0).getPhysicalNumberOfCells()).as("헤더 셀").isPositive();
            }
        }
    }

    @Test
    @DisplayName("데이터사전 : 메모리 유지 행 수(100)를 넘는 데이터도 순서대로 쓰고, 템플릿 헤더/열 너비를 유지한다")
    void dictionaryExport() throws IOException {
        List<StandardTermDto> terms = IntStream.rangeClosed(1, 250)
                .mapToObj(i -> StandardTermDto.builder().id((long) i).commonStandardTermName("용어" + i)
                        .commonStandardTermAbbreviation("TERM_" + i).build())
                .toList();
        List<StandardWordDto> words = List.of(StandardWordDto.builder().id(1L).commonStandardWordName("사용자")
                .commonStandardWordAbbreviation("USER").isFormatWord("N").build());
        List<StandardDomainDto> domains = List.of(StandardDomainDto.builder().id(1L).commonStandardDomainName("명V100")
                .dataType("VARCHAR").dataLength(100).build());

        String headerBefore;
        int widthBefore;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("static/excel/template.xlsx");
             Workbook template = new XSSFWorkbook(in)) {
            headerBefore = template.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
            widthBefore = template.getSheetAt(0).getColumnWidth(2);
        }

        File file = ExcelExportUtils.loadAndCopyDictionaryExcelFile("dictionary", terms::forEach, words::forEach, domains::forEach);

        try (Workbook workbook = open(file)) {
            Sheet termSheet = workbook.getSheetAt(0);
            assertThat(termSheet.getRow(0).getCell(0).getStringCellValue()).as("헤더 유지").isEqualTo(headerBefore);
            assertThat(termSheet.getColumnWidth(2)).as("열 너비 유지").isEqualTo(widthBefore);
            assertThat(termSheet.getLastRowNum()).isEqualTo(250);

            Row first = termSheet.getRow(1);
            assertThat(first.getCell(0).getStringCellValue()).isEqualTo("1");
            assertThat(first.getCell(2).getStringCellValue()).isEqualTo("용어1");
            assertThat(first.getCell(3).getStringCellValue()).as("null 은 빈 문자열").isEmpty();
            assertThat(first.getHeightInPoints()).as("데이터 행 높이").isEqualTo(15.75f);
            assertThat(termSheet.getRow(250).getCell(4).getStringCellValue()).isEqualTo("TERM_250");

            assertThat(workbook.getSheetAt(1).getRow(1).getCell(3).getStringCellValue()).isEqualTo("USER");
            assertThat(workbook.getSheetAt(2).getRow(1).getCell(7).getStringCellValue()).isEqualTo("100");
        }
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"functionSpec", "interfaceSpec", "technicalSpec", "unitTest"})
    @DisplayName("설계서/테스트 시나리오 : 필드 순서대로 열을 채우고 빈 필드명은 빈 셀로 둔다")
    void designExport(String formatType) throws IOException {
        List<CodeRuleDetailDto> rules = List.of(
                CodeRuleDetailDto.builder().methodKeyword("user").codeRuleName("조회").methodName("selectUser")
                        .methodPurpose("사용자를 조회한다").input("id").build(),
                CodeRuleDetailDto.builder().methodName("deleteUser").build());

        File file = ExcelExportUtils.loadAndCopyExcelFile(formatType, rules::forEach,
                List.of("methodKeyword|codeRuleName", "methodName", "", "input"));

        try (Workbook workbook = open(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(2);

            Row row = sheet.getRow(1);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("user조회");
            assertThat(row.getCell(1).getStringCellValue()).isEqualTo("selectUser");
            assertThat(row.getCell(2).getStringCellValue()).isEmpty();
            assertThat(row.getCell(3).getStringCellValue()).isEqualTo("id");
            assertThat(sheet.getRow(2).getCell(3).getStringCellValue()).as("null 은 빈 문자열").isEmpty();
        }
    }
}
