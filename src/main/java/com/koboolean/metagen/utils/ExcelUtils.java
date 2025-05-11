package com.koboolean.metagen.utils;

import com.koboolean.metagen.data.code.domain.dto.CodeRuleDetailDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
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

import java.io.*;
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

    public static File loadAndCopyDictionaryExcelFile(String fileName, Map<String, Object> dataList) throws IOException {
        InputStream inputStream = ExcelUtils.class.getClassLoader().getResourceAsStream("static/excel/template.xlsx");

        if (inputStream == null) {
            throw new FileNotFoundException("파일을 찾을 수 없습니다: " + fileName);
        }

        // 임시파일로 복사 (수정 가능)
        File tempFile = File.createTempFile("temp-", ".xlsx");
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            inputStream.transferTo(out);
        }

        FileInputStream fileInputStream = new FileInputStream(tempFile);
        Workbook workbook = new XSSFWorkbook(fileInputStream);


        // 4. 데이터 입력 (헤더 다음 줄부터)
        int rowNum = 1;
        Sheet sheet = workbook.getSheetAt(0);

        List<StandardTermDto> standardTermDtos = (List<StandardTermDto>) dataList.get("termDtos");

        for(StandardTermDto standardTermDto : standardTermDtos){
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(standardTermDto.getId() != 0 ? standardTermDto.getId() + "" : "");
            row.createCell(1).setCellValue(standardTermDto.getRevisionNumber() != 0 ? standardTermDto.getRevisionNumber() + "" : "");
            row.createCell(2).setCellValue(standardTermDto.getCommonStandardTermName() != null ? standardTermDto.getCommonStandardTermName() : "");
            row.createCell(3).setCellValue(standardTermDto.getCommonStandardTermDescription() != null ? standardTermDto.getCommonStandardTermDescription() : "");
            row.createCell(4).setCellValue(standardTermDto.getCommonStandardTermAbbreviation() != null ? standardTermDto.getCommonStandardTermAbbreviation() : "");
            row.createCell(5).setCellValue(standardTermDto.getCommonStandardDomainName() != null ? standardTermDto.getCommonStandardDomainName() : "");
            row.createCell(6).setCellValue(standardTermDto.getAllowedValues() != null ? standardTermDto.getAllowedValues() : "");
            row.createCell(7).setCellValue(standardTermDto.getStorageFormat() != null ? standardTermDto.getStorageFormat() : "");
            row.createCell(8).setCellValue(standardTermDto.getDisplayFormat() != null ? standardTermDto.getDisplayFormat() + "" : "");
            row.createCell(9).setCellValue(standardTermDto.getAdministrativeStandardCodeName() != null ? standardTermDto.getAdministrativeStandardCodeName() : "");
            row.createCell(10).setCellValue(standardTermDto.getResponsibleOrganization() != null ? standardTermDto.getResponsibleOrganization() : "");
            row.createCell(11).setCellValue(standardTermDto.getSynonyms() != null ? standardTermDto.getSynonyms()  : "");
        }

        rowNum = 1;
        sheet = workbook.getSheetAt(1);
        List<StandardWordDto> standardWordDtos = (List<StandardWordDto>) dataList.get("wordDtos");

        for(StandardWordDto standardWordDto : standardWordDtos){
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(standardWordDto.getId() != 0 ? standardWordDto.getId() + "" : "");
            row.createCell(1).setCellValue(standardWordDto.getRevisionNumber() != 0 ? standardWordDto.getRevisionNumber() + "" : "");
            row.createCell(2).setCellValue(standardWordDto.getCommonStandardWordName() != null ? standardWordDto.getCommonStandardWordName() : "");
            row.createCell(3).setCellValue(standardWordDto.getCommonStandardWordAbbreviation() != null ? standardWordDto.getCommonStandardWordAbbreviation() : "");
            row.createCell(4).setCellValue(standardWordDto.getCommonStandardWordEnglishName() != null ? standardWordDto.getCommonStandardWordEnglishName() : "");
            row.createCell(5).setCellValue(standardWordDto.getCommonStandardWordDescription() != null ? standardWordDto.getCommonStandardWordDescription() : "");
            row.createCell(6).setCellValue(standardWordDto.getIsFormatWord() != null ? standardWordDto.getIsFormatWord() : "");
            row.createCell(7).setCellValue(standardWordDto.getCommonStandardDomainCategory() != null ? standardWordDto.getCommonStandardDomainCategory() : "");
            row.createCell(8).setCellValue(standardWordDto.getSynonyms() != null ? standardWordDto.getSynonyms() : "");
            row.createCell(9).setCellValue(standardWordDto.getRestrictedWords() != null ? standardWordDto.getRestrictedWords() : "");
        }

        rowNum = 1;
        sheet = workbook.getSheetAt(2);
        List<StandardDomainDto> standardDomainDtos = (List<StandardDomainDto>) dataList.get("domainDtos");

        for(StandardDomainDto standardDomainDto : standardDomainDtos){
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(standardDomainDto.getId() != 0 ? standardDomainDto.getId() + "" : "");
            row.createCell(1).setCellValue(standardDomainDto.getRevisionNumber() != 0 ? standardDomainDto.getRevisionNumber() + "" : "");
            row.createCell(2).setCellValue(standardDomainDto.getCommonStandardDomainGroupName() != null ? standardDomainDto.getCommonStandardDomainGroupName() : "");
            row.createCell(3).setCellValue(standardDomainDto.getCommonStandardDomainCategory() != null ? standardDomainDto.getCommonStandardDomainCategory() : "");
            row.createCell(4).setCellValue(standardDomainDto.getCommonStandardDomainName() != null ? standardDomainDto.getCommonStandardDomainName() : "");
            row.createCell(5).setCellValue(standardDomainDto.getCommonStandardDomainDescription() != null ? standardDomainDto.getCommonStandardDomainDescription() : "");
            row.createCell(6).setCellValue(standardDomainDto.getDataType() != null ? standardDomainDto.getDataType() : "");
            row.createCell(7).setCellValue(standardDomainDto.getDataLength() != null ? standardDomainDto.getDataLength() + "" : "");
            row.createCell(8).setCellValue(standardDomainDto.getDataDecimalLength() != null ? standardDomainDto.getDataDecimalLength() + "" : "");
            row.createCell(9).setCellValue(standardDomainDto.getStorageFormat() != null ? standardDomainDto.getStorageFormat() : "");
            row.createCell(10).setCellValue(standardDomainDto.getDisplayFormat() != null ? standardDomainDto.getDisplayFormat() : "");
            row.createCell(11).setCellValue(standardDomainDto.getUnit() != null ? standardDomainDto.getUnit() : "");
            row.createCell(12).setCellValue(standardDomainDto.getAllowedValues() != null ? standardDomainDto.getAllowedValues() : "");
        }

        // 5. 덮어쓰기 저장
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            workbook.write(fos);
        }

        workbook.close();
        fileInputStream.close();

        return tempFile;
    }

    public static File loadAndCopyExcelFile(String fileName, List<CodeRuleDetailDto> dataList, List<String> data) throws IOException {
        InputStream inputStream = ExcelUtils.class.getClassLoader().getResourceAsStream("static/excel/" + fileName + ".xlsx");

        if (inputStream == null) {
            throw new FileNotFoundException("파일을 찾을 수 없습니다: " + fileName);
        }

        // 임시파일로 복사 (수정 가능)
        File tempFile = File.createTempFile("temp-", ".xlsx");
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            inputStream.transferTo(out);
        }

        FileInputStream fileInputStream = new FileInputStream(tempFile);
        Workbook workbook = new XSSFWorkbook(fileInputStream);
        Sheet sheet = workbook.getSheetAt(0); // 첫 시트 사용

        // 4. 데이터 입력 (헤더 다음 줄부터)
        int rowNum = 1;
        for (CodeRuleDetailDto dto : dataList) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < data.size(); i++) {
                String fieldName = data.get(i);
                String value = "";

                if (fieldName == null || fieldName.isEmpty()) {
                    value = "";
                } else {
                    switch (fieldName) {
                        case "functionGroup": value = dto.getFunctionGroup(); break;
                        case "methodKeyword": value = dto.getMethodKeyword(); break;
                        case "templateName": value = dto.getTemplateName(); break;
                        case "codeRuleName": value = dto.getCodeRuleName(); break;
                        case "methodPurpose": value = dto.getMethodPurpose(); break;
                        case "methodName": value = dto.getMethodName(); break;
                        case "input": value = dto.getInput(); break;
                        case "output": value = dto.getOutput(); break;
                        case "exception": value = dto.getException(); break;
                        case "methodKeyword|codeRuleName": value = dto.getMethodKeyword() + dto.getCodeRuleName(); break;
                        default: value = "";
                    }
                }

                row.createCell(i).setCellValue(value != null ? value : "");
            }
        }

        // 5. 덮어쓰기 저장
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            workbook.write(fos);
        }

        workbook.close();
        fileInputStream.close();

        return tempFile;
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
