package com.koboolean.metagen.design.excel.util;

import com.koboolean.metagen.design.code.domain.dto.CodeRuleDetailDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * 설계서/테스트/데이터사전 엑셀 템플릿에 데이터를 채워 출력한다.
 * (공통 엑셀 유틸은 common 의 ExcelUtils 사용)
 */
public class ExcelExportUtils {

    public static File loadAndCopyDictionaryExcelFile(String fileName, Map<String, Object> dataList) throws IOException {
        InputStream inputStream = ExcelExportUtils.class.getClassLoader().getResourceAsStream("static/excel/template.xlsx");

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
        InputStream inputStream = ExcelExportUtils.class.getClassLoader().getResourceAsStream("static/excel/" + fileName + ".xlsx");

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
}
