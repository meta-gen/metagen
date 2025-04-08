package com.koboolean.metagen.utils;

import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    public static byte[] createExcelFile(String sheetName, List<String[]> data){
        try(Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            Sheet sheet = workbook.createSheet(sheetName);
            int rowNum = 0;

            for (String[] rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(rowData[i]);
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new CustomException(ErrorCode.CREATE_EXCEL_FAIL_EXCEPTION);
        }
    }

    /**
     * /excel 에 위치한 파일을 다운로드 받는다.
     * @param fileName
     * @return
     */
    public static Resource downloadExcelFile(String fileName) throws IOException {
        InputStream inputStream = ExcelUtils.class.getClassLoader().getResourceAsStream("static/excel/" + fileName + ".xlsx");

        // 파일이 존재하는지 확인
        if (inputStream == null) {
            throw new FileNotFoundException("파일을 찾을 수 없습니다: " + fileName);
        }

        return new InputStreamResource(inputStream);
    }

    public static List<Map<String, String>> parseExcelFile(MultipartFile file, int sheetNum, List<String> predefinedHeaders, boolean useFirstRowAsHeader, int startRow) throws IOException {
        List<Map<String, String>> dataList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(sheetNum);
            if (sheet == null) throw new IllegalArgumentException("Excel 파일에 시트가 존재하지 않습니다.");

            List<String> headers = new ArrayList<>(predefinedHeaders); // 미리 정의한 헤더 사용

            if (useFirstRowAsHeader) {
                // 첫 번째 행을 헤더로 사용하는 경우
                Row headerRow = sheet.getRow(0);
                headers.clear();
                for (Cell cell : headerRow) {
                    headers.add(cell.getStringCellValue());
                }
                startRow = 1; // 데이터는 두 번째 행부터 시작
            }

            // 데이터 행 읽기
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowData.put(headers.get(j), getCellValue(cell));
                }
                dataList.add(rowData);
            }
        }
        return dataList;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

}
