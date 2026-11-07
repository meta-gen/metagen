package com.koboolean.metagen;

import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.support.SecurityTestSupport;
import com.koboolean.metagen.data.column.repository.ColumnInfoRepository;
import com.koboolean.metagen.data.column.service.ColumnManageService;
import com.koboolean.metagen.data.dictionary.service.DataDictionaryService;
import com.koboolean.metagen.data.table.service.TableDesignService;
import jakarta.persistence.EntityManagerFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.function.IntFunction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 엑셀 업로드 성능 측정 (기본 빌드에서는 실행하지 않는다)
 *
 *   METAGEN_PERF=true ./gradlew :metagen-app:test --tests 'com.koboolean.metagen.UploadPerformanceTest'
 *
 * 실제 업로드 서비스(데이터사전 → 테이블 → 컬럼)를 호출해 단계별 소요 시간과 실행된 SQL 문 수를 출력한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "METAGEN_PERF", matches = "true")
class UploadPerformanceTest {

    private static final long PROJECT_ID = 0L;

    private static final int DOMAINS = 20;
    private static final int WORDS = 4_000;
    private static final int TERMS = 4_000;
    private static final int TABLES = 2_000;
    private static final int COLUMNS_PER_TABLE = 2;

    @Autowired DataDictionaryService dataDictionaryService;
    @Autowired TableDesignService tableDesignService;
    @Autowired ColumnManageService columnManageService;
    @Autowired ColumnInfoRepository columnInfoRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired EntityManagerFactory entityManagerFactory;

    @AfterEach
    void tearDown() {
        SecurityTestSupport.clear();
    }

    @Test
    @DisplayName("데이터사전(도메인/단어/용어) → 테이블 → 컬럼 업로드 소요 시간")
    void upload() throws IOException {
        AccountDto login = SecurityTestSupport.loginAs(projectRepository, PROJECT_ID, "1", "ROLE_ADMIN");
        String p = randomPrefix();

        Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);

        // 시트 순서는 실제 템플릿과 같다 : 0 용어, 1 단어, 2 도메인
        byte[] dictionary = workbook(
                sheet(TERMS, i -> {
                    int d = i % DOMAINS;
                    return new String[]{"", "1", p + "단어" + i + " " + p + "단어" + d, "용어 설명",
                            p + "W" + i + "_" + p + "W" + d, p + "도메인" + d, "", "", "", "", "", ""};
                }),
                sheet(WORDS, i -> new String[]{"", "1", p + "단어" + i, p + "W" + i, "Word " + i, "단어 설명", "N",
                        i < DOMAINS ? p + "분류" + i : "-", "", ""}),
                sheet(DOMAINS, i -> new String[]{"", "1", "문자", p + "분류" + i, p + "도메인" + i, "도메인 설명",
                        "VARCHAR", "100", "0", "", "", "", ""}));

        byte[] tables = workbook(sheet(TABLES, i -> new String[]{p + "_T" + i, "테이블 " + i, "N", String.valueOf(i)}));

        byte[] columns = workbook(sheet(TABLES * COLUMNS_PER_TABLE, i -> {
            int table = i / COLUMNS_PER_TABLE;
            int term = (table * COLUMNS_PER_TABLE + i % COLUMNS_PER_TABLE) % TERMS;
            return new String[]{p + "_T" + table, p + "W" + term + "_" + p + "W" + (term % DOMAINS), "컬럼 설명", "VARCHAR",
                    "100", "0", "0", "Y", "", String.valueOf(i % COLUMNS_PER_TABLE + 1), "N", "", "N", "N", "N", "N", "N", "N", ""};
        }));

        long columnsBefore = columnInfoRepository.count();

        measure(stats, "데이터사전 (도메인 " + DOMAINS + " / 단어 " + WORDS + " / 용어 " + TERMS + ")",
                () -> dataDictionaryService.uploadDictionaryExcelFile(file(dictionary), login));
        measure(stats, "테이블 " + TABLES,
                () -> tableDesignService.uploadTableExcelFile(file(tables), login));
        measure(stats, "컬럼 " + TABLES * COLUMNS_PER_TABLE,
                () -> columnManageService.uploadColumnExcelFile(file(columns), login));

        assertThat(columnInfoRepository.count() - columnsBefore).isEqualTo((long) TABLES * COLUMNS_PER_TABLE);
    }

    private interface Upload {
        void run() throws IOException;
    }

    private static void measure(Statistics stats, String name, Upload upload) throws IOException {
        stats.clear();
        long start = System.nanoTime();
        upload.run();
        long millis = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("[UPLOAD-PERF] %-45s %,7d ms | SQL 문 %,7d | INSERT 엔티티 %,7d | 조회 %,6d%n",
                name, millis, stats.getPrepareStatementCount(), stats.getEntityInsertCount(), stats.getQueryExecutionCount());
    }

    private static String randomPrefix() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("P");
        for (int i = 0; i < 5; i++) sb.append((char) ('A' + random.nextInt(26)));
        return sb.toString();
    }

    private record SheetData(int rows, IntFunction<String[]> row) {
    }

    private static SheetData sheet(int rows, IntFunction<String[]> row) {
        return new SheetData(rows, row);
    }

    /** 헤더 1행 + 데이터 행으로 된 xlsx (테스트 데이터 생성은 SXSSF 스트리밍 쓰기) */
    private static byte[] workbook(SheetData... sheets) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (SheetData data : sheets) {
                Sheet sheet = workbook.createSheet();
                sheet.createRow(0).createCell(0).setCellValue("header");
                for (int i = 0; i < data.rows(); i++) {
                    Row row = sheet.createRow(i + 1);
                    String[] values = data.row().apply(i);
                    for (int c = 0; c < values.length; c++) row.createCell(c).setCellValue(values[c]);
                }
            }
            workbook.write(out);
            return out.toByteArray();
        } finally {
            workbook.close();
        }
    }

    private static MockMultipartFile file(byte[] bytes) {
        return new MockMultipartFile("file", "upload.xlsx", null, bytes);
    }
}
