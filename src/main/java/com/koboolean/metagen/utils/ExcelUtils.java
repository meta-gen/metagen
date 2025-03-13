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
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        ClassPathResource classPathResource = new ClassPathResource("static/excel/" + fileName + ".xlsx");

        // 파일이 존재하는지 확인
        if (!classPathResource.exists()) {
            throw new FileNotFoundException("파일을 찾을 수 없습니다: " + fileName);
        }

        // ✅ InputStream을 사용하여 파일을 읽기
        InputStream inputStream = classPathResource.getInputStream();
        InputStreamResource resource = new InputStreamResource(inputStream);


        return resource;
    }

}
