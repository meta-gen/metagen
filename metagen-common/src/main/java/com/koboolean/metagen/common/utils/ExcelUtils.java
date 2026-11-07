package com.koboolean.metagen.common.utils;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import org.apache.poi.UnsupportedFileFormatException;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 엑셀 공통 유틸
 *
 * 메모리에 파일 전체를 올리지 않는다.
 * - 읽기 : 업로드 파일을 임시 파일로 복사한 뒤 시트 XML 을 SAX 로 한 행씩 읽어 콜백으로 넘긴다. (XSSFWorkbook 사용 금지)
 * - 쓰기 : SXSSFWorkbook 으로 일정 행 수만 메모리에 두고 나머지는 임시 파일로 내보낸다.
 */
public class ExcelUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    /** SXSSF 쓰기 시 메모리에 유지할 행 수 */
    private static final int STREAMING_WINDOW_SIZE = 100;

    /** 업로드 저장 청크 크기 : hibernate.jdbc.batch_size(application.yml)와 같게 둔다 */
    public static final int UPLOAD_CHUNK_SIZE = 500;

    public static byte[] createExcelFile(String sheetName, List<String[]> data){
        SXSSFWorkbook workbook = new SXSSFWorkbook(STREAMING_WINDOW_SIZE);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            int rowNum = 0;

            for (String[] rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.length; i++) {
                    row.createCell(i).setCellValue(rowData[i]);
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new CustomException(ErrorCode.CREATE_EXCEL_FAIL_EXCEPTION);
        } finally {
            closeQuietly(workbook); // SXSSF 임시 파일 정리 포함
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

    /**
     * 엑셀 시트를 한 행씩 읽어 rowHandler 로 넘긴다. (행 전체를 목록으로 모으지 않는다)
     *
     * @param file       업로드 파일 (.xlsx)
     * @param sheetIndex 읽을 시트 순번 (0부터)
     * @param headers    열 순서대로의 키 (A열 = headers[0]) - 각 행은 이 키 순서의 Map 으로 넘어간다
     * @param startRow   읽기 시작할 행 번호 (0부터, 보통 헤더 다음 행인 1)
     * @param rowHandler 행 처리 (값이 없는 셀은 빈 문자열, 숫자는 정수 문자열, 수식은 수식 문자열, 논리값은 true/false)
     *                   rowHandler 에서 던진 예외는 그대로 전달되며 이후 행은 읽지 않는다.
     */
    public static void readExcelRows(MultipartFile file, int sheetIndex, List<String> headers, int startRow,
                                     Consumer<Map<String, String>> rowHandler) throws IOException {
        Path tempFile = Files.createTempFile("excel-upload-", ".xlsx");
        try {
            // 업로드 파일을 스트림으로 임시 파일에 복사한다. (OPCPackage 를 InputStream 으로 열면 전체를 메모리에 올린다)
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            try (OPCPackage pkg = OPCPackage.open(tempFile.toFile(), PackageAccess.READ)) {
                XSSFReader reader = new XSSFReader(pkg);
                ReadOnlySharedStringsTable sharedStrings = new ReadOnlySharedStringsTable(pkg);

                try (InputStream sheet = openSheet(reader, sheetIndex)) {
                    XMLReader parser = XMLHelper.newXMLReader();
                    parser.setContentHandler(new SheetRowHandler(sharedStrings, headers, startRow, rowHandler));
                    parser.parse(new InputSource(sheet));
                }
            } catch (InvalidFormatException | UnsupportedFileFormatException e) {
                throw new IOException("엑셀(.xlsx) 파일 형식이 아닙니다.", e);
            } catch (OpenXML4JException | SAXException | ParserConfigurationException e) {
                // rowHandler 에서 던진 업무 예외(CustomException 등)는 그대로 전달한다.
                if (e.getCause() instanceof RuntimeException runtime) throw runtime;
                throw new IOException("엑셀 파일을 읽을 수 없습니다.", e);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * 엑셀 행을 chunkSize 개씩 묶어 chunkHandler 로 넘긴다. (메모리에는 한 청크만 유지)
     *
     * 업로드 저장은 청크 단위로 한다 : 청크마다 참조/중복 조회를 IN 한 번으로 끝내고 JDBC 배치로 INSERT 한 뒤
     * 영속성 컨텍스트를 비운다. (행마다 조회하면 쌓인 INSERT 가 조회 전에 먼저 실행되어 배치가 끊긴다)
     */
    public static void readExcelRowsInChunks(MultipartFile file, int sheetIndex, List<String> headers, int startRow,
                                             int chunkSize, Consumer<List<Map<String, String>>> chunkHandler) throws IOException {
        List<Map<String, String>> chunk = new ArrayList<>(chunkSize);

        readExcelRows(file, sheetIndex, headers, startRow, row -> {
            chunk.add(row);
            if (chunk.size() >= chunkSize) {
                chunkHandler.accept(List.copyOf(chunk));
                chunk.clear();
            }
        });

        if (!chunk.isEmpty()) chunkHandler.accept(List.copyOf(chunk));
    }

    private static InputStream openSheet(XSSFReader reader, int sheetIndex) throws IOException, OpenXML4JException {
        XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();

        int index = 0;
        while (sheets.hasNext()) {
            InputStream sheet = sheets.next();
            if (index++ == sheetIndex) return sheet;
            sheet.close();
        }

        throw new IllegalArgumentException("Excel 파일에 " + (sheetIndex + 1) + "번째 시트가 존재하지 않습니다.");
    }

    private static void closeQuietly(SXSSFWorkbook workbook) {
        try {
            workbook.close();
        } catch (IOException e) {
            logger.warn("엑셀 임시 파일 정리 실패: {}", e.getMessage());
        }
    }

    /**
     * 시트 XML(&lt;row&gt;/&lt;c&gt;)을 읽어 한 행씩 Map 으로 넘기는 SAX 핸들러
     */
    private static class SheetRowHandler extends DefaultHandler {

        private final ReadOnlySharedStringsTable sharedStrings;
        private final List<String> headers;
        private final int startRow;
        private final Consumer<Map<String, String>> rowHandler;

        private String[] rowValues;
        private int rowIndex = -1;
        private int nextColumn;       // r 속성이 없는 셀의 열 위치

        private int cellColumn;
        private String cellType;
        private boolean hasFormula;
        private final StringBuilder valueText = new StringBuilder();
        private final StringBuilder formulaText = new StringBuilder();
        private final StringBuilder inlineText = new StringBuilder();
        private StringBuilder target;   // 현재 읽고 있는 텍스트 (v / f / is>t)

        SheetRowHandler(ReadOnlySharedStringsTable sharedStrings, List<String> headers, int startRow,
                        Consumer<Map<String, String>> rowHandler) {
            this.sharedStrings = sharedStrings;
            this.headers = headers;
            this.startRow = startRow;
            this.rowHandler = rowHandler;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            switch (localName) {
                case "row" -> {
                    String r = attributes.getValue("r");
                    rowIndex = r != null ? Integer.parseInt(r) - 1 : rowIndex + 1;
                    rowValues = new String[headers.size()];
                    nextColumn = 0;
                }
                case "c" -> {
                    String ref = attributes.getValue("r");
                    cellColumn = ref != null ? new CellReference(ref).getCol() : nextColumn;
                    nextColumn = cellColumn + 1;
                    cellType = attributes.getValue("t");
                    hasFormula = false;
                    valueText.setLength(0);
                    formulaText.setLength(0);
                    inlineText.setLength(0);
                }
                case "v" -> target = valueText;
                case "f" -> {
                    hasFormula = true;
                    target = formulaText;
                }
                case "t" -> target = "inlineStr".equals(cellType) ? inlineText : null;
                default -> { }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (target != null) target.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            switch (localName) {
                case "v", "f", "t" -> target = null;
                case "c" -> {
                    if (rowValues != null && cellColumn < rowValues.length) {
                        rowValues[cellColumn] = cellValue();
                    }
                }
                case "row" -> {
                    if (rowIndex >= startRow) {
                        Map<String, String> row = new LinkedHashMap<>();
                        for (int i = 0; i < headers.size(); i++) {
                            row.put(headers.get(i), rowValues[i] == null ? "" : rowValues[i]);
                        }
                        rowHandler.accept(row);
                    }
                    rowValues = null;
                }
                default -> { }
            }
        }

        /**
         * 기존 XSSFWorkbook 방식과 같은 규칙으로 셀 값을 만든다.
         */
        private String cellValue() {
            if (hasFormula) return formulaText.toString();

            String value = valueText.toString();
            if (cellType == null || "n".equals(cellType)) {
                if (value.isEmpty()) return "";
                try {
                    return String.valueOf((long) Double.parseDouble(value)); // 숫자는 정수로
                } catch (NumberFormatException e) {
                    return value;
                }
            }

            return switch (cellType) {
                case "s" -> value.isEmpty() ? "" : sharedStrings.getItemAt(Integer.parseInt(value)).getString();
                case "inlineStr" -> inlineText.toString();
                case "str" -> value;
                case "b" -> "1".equals(value) ? "true" : "false";
                default -> ""; // e(오류) 등
            };
        }
    }
}
