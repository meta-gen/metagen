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

import java.io.ByteArrayOutputStream;
import java.util.List;

public class PoiUtils {

    private static final Logger logger = LoggerFactory.getLogger(PoiUtils.class);

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
     * path에 위치한 파일을 다운로드 받는다.
     * @param path
     * @return
     */
    public static byte[] downloadExcelFile(String path){
        return null;
    }

}
