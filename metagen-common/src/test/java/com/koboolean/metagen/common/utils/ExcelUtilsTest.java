package com.koboolean.metagen.common.utils;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelUtilsTest {

    private static MockMultipartFile toMultipart(byte[] bytes) {
        return new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);
    }

    private static List<Map<String, String>> readAll(byte[] bytes, int sheetIndex, List<String> headers, int startRow) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        ExcelUtils.readExcelRows(toMultipart(bytes), sheetIndex, headers, startRow, rows::add);
        return rows;
    }

    @Test
    @DisplayName("createExcelFile(SXSSF 스트리밍 쓰기)로 만든 파일을 스트리밍 읽기로 다시 읽을 수 있다 (인라인 문자열)")
    void createAndRead() throws IOException {
        byte[] bytes = ExcelUtils.createExcelFile("sheet", List.of(
                new String[]{"name", "desc"},
                new String[]{"USER", "사용자"},
                new String[]{"ROLE", "권한"}));

        assertThat(readAll(bytes, 0, List.of("name", "desc"), 1)).containsExactly(
                Map.of("name", "USER", "desc", "사용자"),
                Map.of("name", "ROLE", "desc", "권한"));
    }

    @Test
    @DisplayName("미리 정의한 헤더와 시작 행을 사용하고, 숫자는 정수 문자열 / 빈 셀은 빈 문자열 / 수식은 수식 문자열로 읽는다")
    void readWithPredefinedHeaders() throws IOException {
        byte[] bytes;
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("무시되는 헤더");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("TABLE_A");   // 공유 문자열
            row.createCell(1).setCellValue(12.7);        // 숫자 → 정수
            // 2번 셀은 비워둔다
            row.createCell(3).setCellValue(true);
            row.createCell(4).setCellFormula("1+1");
            workbook.write(out);
            bytes = out.toByteArray();
        }

        List<Map<String, String>> rows = readAll(bytes, 0,
                List.of("tableName", "sortOrder", "description", "isMaster", "formula"), 1);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0))
                .containsEntry("tableName", "TABLE_A")
                .containsEntry("sortOrder", "12")
                .containsEntry("description", "")
                .containsEntry("isMaster", "true")
                .containsEntry("formula", "1+1");
        // 헤더 순서가 유지된다
        assertThat(rows.get(0).keySet()).containsExactly("tableName", "sortOrder", "description", "isMaster", "formula");
    }

    @Test
    @DisplayName("지정한 순번의 시트를 읽고, 빈 행(행 자체가 없는 경우)은 건너뛴다")
    void readSheetBySheetIndex() throws IOException {
        byte[] bytes;
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.createSheet("first").createRow(1).createCell(0).setCellValue("FIRST");
            Sheet second = workbook.createSheet("second");
            second.createRow(1).createCell(0).setCellValue("A");
            second.createRow(5).createCell(1).setCellValue("B"); // 2~4행은 없음
            workbook.write(out);
            bytes = out.toByteArray();
        }

        assertThat(readAll(bytes, 1, List.of("c1", "c2"), 1)).containsExactly(
                Map.of("c1", "A", "c2", ""),
                Map.of("c1", "", "c2", "B"));
    }

    @Test
    @DisplayName("행 처리 중 던진 업무 예외는 그대로 전달되고 이후 행은 읽지 않는다")
    void handlerExceptionStopsReading() {
        byte[] bytes = ExcelUtils.createExcelFile("sheet", List.of(
                new String[]{"name"}, new String[]{"A"}, new String[]{"B"}, new String[]{"C"}));

        List<String> handled = new ArrayList<>();

        assertThatThrownBy(() -> ExcelUtils.readExcelRows(toMultipart(bytes), 0, List.of("name"), 1, row -> {
            handled.add(row.get("name"));
            if ("B".equals(row.get("name"))) throw new CustomException(ErrorCode.TABLE_IS_NOT_DEFINED, "B");
        }))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TABLE_IS_NOT_DEFINED);

        assertThat(handled).containsExactly("A", "B");
    }

    @Test
    @DisplayName("청크 단위 읽기 : 지정한 크기씩 묶어 순서대로 넘기고 마지막 남은 행도 넘긴다")
    void readInChunks() throws IOException {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"name"});
        for (int i = 1; i <= 1_201; i++) data.add(new String[]{"R" + i});
        byte[] bytes = ExcelUtils.createExcelFile("sheet", data);

        List<Integer> chunkSizes = new ArrayList<>();
        List<String> names = new ArrayList<>();
        ExcelUtils.readExcelRowsInChunks(toMultipart(bytes), 0, List.of("name"), 1, 500, chunk -> {
            chunkSizes.add(chunk.size());
            chunk.forEach(row -> names.add(row.get("name")));
        });

        assertThat(chunkSizes).containsExactly(500, 500, 201);
        assertThat(names).hasSize(1_201).startsWith("R1", "R2").endsWith("R1201");
    }

    @Test
    @DisplayName("없는 시트 순번이면 IllegalArgumentException")
    void missingSheet() {
        byte[] bytes = ExcelUtils.createExcelFile("sheet", List.<String[]>of(new String[]{"x"}));

        assertThatThrownBy(() -> readAll(bytes, 3, List.of("x"), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("xlsx 가 아닌 파일은 IOException")
    void notExcel() {
        assertThatThrownBy(() -> readAll("not excel".getBytes(), 0, List.of("x"), 0))
                .isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("존재하지 않는 엑셀 템플릿은 FileNotFoundException")
    void downloadMissingTemplate() {
        assertThatThrownBy(() -> ExcelUtils.downloadExcelFile("not-exists-template"))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("not-exists-template");
    }
}
