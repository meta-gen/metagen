package com.koboolean.metagen.common.utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelUtilsTest {

    private static MockMultipartFile toMultipart(byte[] bytes) {
        return new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);
    }

    @Test
    @DisplayName("createExcelFile 로 만든 파일을 첫 행 헤더 기준으로 다시 읽을 수 있다")
    void createAndParseWithHeaderRow() throws IOException {
        byte[] bytes = ExcelUtils.createExcelFile("sheet", List.of(
                new String[]{"name", "desc"},
                new String[]{"USER", "사용자"},
                new String[]{"ROLE", "권한"}));

        List<Map<String, String>> rows = ExcelUtils.parseExcelFile(toMultipart(bytes), 0, List.of(), true, 99);

        assertThat(rows).containsExactly(
                Map.of("name", "USER", "desc", "사용자"),
                Map.of("name", "ROLE", "desc", "권한"));
    }

    @Test
    @DisplayName("미리 정의한 헤더와 시작 행을 사용하고, 숫자는 정수 문자열 / 빈 셀은 빈 문자열로 읽는다")
    void parseWithPredefinedHeaders() throws IOException {
        byte[] bytes;
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("무시되는 헤더");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("TABLE_A");
            row.createCell(1).setCellValue(12.0);
            // 2번 셀은 비워둔다
            row.createCell(3).setCellValue(true);
            workbook.write(out);
            bytes = out.toByteArray();
        }

        List<Map<String, String>> rows = ExcelUtils.parseExcelFile(toMultipart(bytes), 0,
                List.of("tableName", "sortOrder", "description", "isMaster"), false, 1);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0))
                .containsEntry("tableName", "TABLE_A")
                .containsEntry("sortOrder", "12")
                .containsEntry("description", "")
                .containsEntry("isMaster", "true");
        // 헤더 순서가 유지된다
        assertThat(rows.get(0).keySet()).containsExactly("tableName", "sortOrder", "description", "isMaster");
    }

    @Test
    @DisplayName("존재하지 않는 엑셀 템플릿은 FileNotFoundException")
    void downloadMissingTemplate() {
        assertThatThrownBy(() -> ExcelUtils.downloadExcelFile("not-exists-template"))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("not-exists-template");
    }
}
