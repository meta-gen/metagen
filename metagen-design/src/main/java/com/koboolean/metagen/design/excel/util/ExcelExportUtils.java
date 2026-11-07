package com.koboolean.metagen.design.excel.util;

import com.koboolean.metagen.design.code.domain.dto.CodeRuleDetailDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;

/**
 * 설계서/테스트/데이터사전 엑셀 템플릿에 데이터를 채워 출력한다.
 * (공통 엑셀 유틸은 common 의 ExcelUtils 사용)
 *
 * 메모리에 전체를 올리지 않도록 SXSSFWorkbook(스트리밍 쓰기)으로 데이터 행을 쓴다.
 * - static/excel 의 출력용 템플릿은 헤더(1행)만 둔다. SXSSF 는 템플릿에 이미 있는 행 위치에 쓸 수 없다.
 * - 반환한 임시 파일은 호출한 쪽에서 다 내려보낸 뒤 지워야 한다. (예: Files.newInputStream(path, DELETE_ON_CLOSE))
 */
public class ExcelExportUtils {

    /** 메모리에 유지할 행 수 (나머지는 임시 파일로 내보낸다) */
    private static final int STREAMING_WINDOW_SIZE = 100;

    /** 데이터 행 높이 : 예전 템플릿에 미리 만들어둔 행의 높이와 같다 */
    private static final float DATA_ROW_HEIGHT = 15.75f;

    /** 데이터는 헤더 다음 줄부터 */
    private static final int FIRST_DATA_ROW = 1;

    /**
     * 출력할 데이터를 한 건씩 넘겨주는 소스. 목록 전체를 들고 있지 않아도 된다.
     * - DB : PagedReader 로 페이지 단위 조회 (ExcelServiceImpl)
     * - 이미 가진 목록 : list::forEach
     */
    @FunctionalInterface
    public interface RowSource<T> {
        void forEach(Consumer<? super T> action);
    }

    /** 다음에 쓸 행 번호 (람다 안에서 증가시키기 위한 카운터) */
    private static final class RowCounter {
        private int next = FIRST_DATA_ROW;

        int next() {
            return next++;
        }
    }

    public static File loadAndCopyDictionaryExcelFile(String fileName, RowSource<StandardTermDto> terms,
                                                      RowSource<StandardWordDto> words, RowSource<StandardDomainDto> domains) throws IOException {
        return export("template", fileName, workbook -> {
            // 표준용어
            Sheet termSheet = workbook.getSheetAt(0);
            RowCounter termRows = new RowCounter();
            terms.forEach(dto ->
                writeRow(termSheet, termRows.next(),
                        dto.getId() != 0 ? dto.getId() + "" : "",
                        dto.getRevisionNumber() != 0 ? dto.getRevisionNumber() + "" : "",
                        dto.getCommonStandardTermName(),
                        dto.getCommonStandardTermDescription(),
                        dto.getCommonStandardTermAbbreviation(),
                        dto.getCommonStandardDomainName(),
                        dto.getAllowedValues(),
                        dto.getStorageFormat(),
                        dto.getDisplayFormat(),
                        dto.getAdministrativeStandardCodeName(),
                        dto.getResponsibleOrganization(),
                        dto.getSynonyms()));

            // 표준단어
            Sheet wordSheet = workbook.getSheetAt(1);
            RowCounter wordRows = new RowCounter();
            words.forEach(dto ->
                writeRow(wordSheet, wordRows.next(),
                        dto.getId() != 0 ? dto.getId() + "" : "",
                        dto.getRevisionNumber() != 0 ? dto.getRevisionNumber() + "" : "",
                        dto.getCommonStandardWordName(),
                        dto.getCommonStandardWordAbbreviation(),
                        dto.getCommonStandardWordEnglishName(),
                        dto.getCommonStandardWordDescription(),
                        dto.getIsFormatWord(),
                        dto.getCommonStandardDomainCategory(),
                        dto.getSynonyms(),
                        dto.getRestrictedWords()));

            // 표준도메인
            Sheet domainSheet = workbook.getSheetAt(2);
            RowCounter domainRows = new RowCounter();
            domains.forEach(dto ->
                writeRow(domainSheet, domainRows.next(),
                        dto.getId() != 0 ? dto.getId() + "" : "",
                        dto.getRevisionNumber() != 0 ? dto.getRevisionNumber() + "" : "",
                        dto.getCommonStandardDomainGroupName(),
                        dto.getCommonStandardDomainCategory(),
                        dto.getCommonStandardDomainName(),
                        dto.getCommonStandardDomainDescription(),
                        dto.getDataType(),
                        dto.getDataLength() != null ? dto.getDataLength() + "" : "",
                        dto.getDataDecimalLength() != null ? dto.getDataDecimalLength() + "" : "",
                        dto.getStorageFormat(),
                        dto.getDisplayFormat(),
                        dto.getUnit(),
                        dto.getAllowedValues()));
        });
    }

    /**
     * 설계서/테스트 시나리오 출력
     * @param fileName 템플릿 파일명 (functionSpec, interfaceSpec, technicalSpec, unitTest)
     * @param rules    출력할 코드규칙
     * @param data     열 순서대로 채울 필드명 (빈 문자열이면 빈 셀)
     */
    public static File loadAndCopyExcelFile(String fileName, RowSource<CodeRuleDetailDto> rules, List<String> data) throws IOException {
        return export(fileName, fileName, workbook -> {
            Sheet sheet = workbook.getSheetAt(0); // 첫 시트 사용
            RowCounter rows = new RowCounter();

            rules.forEach(dto -> {
                String[] values = data.stream().map(fieldName -> fieldValue(dto, fieldName)).toArray(String[]::new);
                writeRow(sheet, rows.next(), values);
            });
        });
    }

    private static String fieldValue(CodeRuleDetailDto dto, String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return "";

        return switch (fieldName) {
            case "functionGroup" -> dto.getFunctionGroup();
            case "methodKeyword" -> dto.getMethodKeyword();
            case "templateName" -> dto.getTemplateName();
            case "codeRuleName" -> dto.getCodeRuleName();
            case "methodPurpose" -> dto.getMethodPurpose();
            case "methodName" -> dto.getMethodName();
            case "input" -> dto.getInput();
            case "output" -> dto.getOutput();
            case "exception" -> dto.getException();
            case "methodKeyword|codeRuleName" -> dto.getMethodKeyword() + dto.getCodeRuleName();
            default -> "";
        };
    }

    /**
     * 템플릿(헤더만 있는 작은 파일)을 열고 SXSSF 로 데이터 행을 써서 임시 파일로 저장한다.
     *
     * @param templateName static/excel/{templateName}.xlsx
     * @param fileName     오류 메시지용 이름
     * @param writer       데이터 행 쓰기
     */
    private static File export(String templateName, String fileName, Consumer<SXSSFWorkbook> writer) throws IOException {
        File outFile = null;

        try (InputStream template = ExcelExportUtils.class.getClassLoader().getResourceAsStream("static/excel/" + templateName + ".xlsx")) {
            if (template == null) {
                throw new FileNotFoundException("파일을 찾을 수 없습니다: " + fileName);
            }

            SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook(template), STREAMING_WINDOW_SIZE);
            try {
                writer.accept(workbook);

                outFile = File.createTempFile("excel-export-", ".xlsx");
                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile))) {
                    workbook.write(out);
                }
                return outFile;
            } finally {
                workbook.close(); // POI 5.x : 스트리밍 임시 파일도 함께 삭제
            }
        } catch (IOException | RuntimeException e) {
            if (outFile != null) Files.deleteIfExists(outFile.toPath());
            throw e;
        }
    }

    private static void writeRow(Sheet sheet, int rowNum, String... values) {
        Row row = sheet.createRow(rowNum);
        row.setHeightInPoints(DATA_ROW_HEIGHT);

        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i] != null ? values[i] : "");
        }
    }
}
