package com.koboolean.metagen;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.security.repository.AccountRepository;
import com.koboolean.metagen.core.support.SecurityTestSupport;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
import com.koboolean.metagen.data.dictionary.service.DataDictionaryService;
import com.koboolean.metagen.data.dictionary.service.StandardTermService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 데이터 사전 엑셀 업로드 : 표준용어명이 영문약어명과 맞지 않을 때 (실제 업로드 경로, H2)
 * - 영문약어명의 표준 단어로 용어명을 만들고, 표준용어설명 끝에 생성 근거를 남긴다. (업로드는 취소하지 않는다)
 * - 약어에 해당하는 표준 단어가 없으면 기존처럼 업로드 전체를 취소한다.
 *
 * 공통표준용어 템플릿(static/excel/dataPortal.xlsx)의 단어/도메인 시트는 그대로 쓰고, 용어 시트만 시험용 행으로 바꿔 올린다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class TermUploadNameGenerationTest {

    // 용어 시트 열 (StandardTermService.setStandardTerm 헤더 순서)
    private static final int COL_NAME = 2;
    private static final int COL_DESCRIPTION = 3;
    private static final int COL_ABBREVIATION = 4;

    @Autowired DataDictionaryService dataDictionaryService;
    @Autowired StandardTermRepository standardTermRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired AccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        SecurityTestSupport.clear();
    }

    @Test
    @DisplayName("용어명이 영문약어명과 맞지 않으면 표준 단어로 용어명을 만들고 설명에 근거를 남긴다 (맞는 행은 그대로)")
    void generateTermNames() throws IOException {
        // 템플릿 용어 GIS_자료_여부 / GIS_DATA_YN , HTML_사용_여부 / HTML_USE_YN 을 바탕으로
        MockMultipartFile file = templateWithTerms(List.of(
                new TermRow("GIS_자료_여부", "그대로 두는 행", "GIS_DATA_YN"),          // 맞음 → 그대로
                new TermRow("GIS자료여부", "구분자 없는 용어명", "GIS_DATA_YN"),         // 구분자 없음 → 생성
                new TermRow("HTML 사용 여부 플래그", "", "HTML_USE_YN")                 // 단어 수 다름 + 설명 없음 → 생성
        ));
        AccountDto login = loginToNewProject("term-upload-generate");

        dataDictionaryService.uploadDictionaryExcelFile(file, login);

        Map<String, StandardTerm> byDescriptionStart = standardTermRepository.findAllByProjectId(login.getProjectId()).stream()
                .collect(Collectors.toMap(term -> term.getCommonStandardTermDescription() == null ? "" : term.getCommonStandardTermDescription().split("\n")[0],
                        Function.identity(), (a, b) -> a));
        assertThat(byDescriptionStart).hasSize(3);

        StandardTerm kept = byDescriptionStart.get("그대로 두는 행");
        assertThat(kept.getCommonStandardTermName()).isEqualTo("GIS_자료_여부");
        assertThat(kept.getCommonStandardTermDescription()).as("맞는 행은 설명을 바꾸지 않는다")
                .doesNotContain(StandardTermService.GENERATED_NOTE_PREFIX);

        StandardTerm noSeparator = byDescriptionStart.get("구분자 없는 용어명");
        assertThat(noSeparator.getCommonStandardTermName()).isEqualTo("GIS_자료_여부");
        assertThat(noSeparator.getCommonStandardTermDescription())
                .startsWith("구분자 없는 용어명\n" + StandardTermService.GENERATED_NOTE_PREFIX)
                .contains("영문약어명(GIS_DATA_YN)")
                .contains("원본 표준용어명: GIS자료여부");

        StandardTerm wordCount = byDescriptionStart.get(StandardTermService.GENERATED_NOTE_PREFIX
                + " 영문약어명(HTML_USE_YN)의 표준 단어로 표준용어명을 생성했습니다. (원본 표준용어명: HTML 사용 여부 플래그)");
        assertThat(wordCount).as("설명이 없으면 생성 근거만 들어간다").isNotNull();
        assertThat(wordCount.getCommonStandardTermName()).isEqualTo("HTML_사용_여부");
        assertThat(wordCount.getCommonStandardTermAbbreviation()).isEqualTo("HTML_USE_YN");
    }

    @Test
    @DisplayName("영문약어에 해당하는 표준 단어가 없으면 기존처럼 업로드 전체를 취소한다 (NOT_FOUND_WORD_DATA)")
    void unknownAbbreviationCancelsUpload() throws IOException {
        MockMultipartFile file = templateWithTerms(List.of(
                new TermRow("GIS_자료_여부", "정상 행", "GIS_DATA_YN"),
                new TermRow("없는 용어", "", "NOT_EXISTS_ABBR_QQ")
        ));
        AccountDto login = loginToNewProject("term-upload-cancel");

        assertThatThrownBy(() -> dataDictionaryService.uploadDictionaryExcelFile(file, login))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND_WORD_DATA);
        assertThat(standardTermRepository.findAllByProjectId(login.getProjectId())).as("용어는 하나도 저장되지 않는다").isEmpty();
    }

    // ------------------------------------------------------------------ 보조

    private record TermRow(String name, String description, String abbreviation) {}

    private AccountDto loginToNewProject(String projectName) {
        Account admin = accountRepository.findAll().stream()
                .filter(account -> "admin".equals(account.getUsername())).findFirst().orElseThrow();
        Project project = projectRepository.save(Project.builder()
                .projectName(projectName).isActive(true).account(admin).build());
        return SecurityTestSupport.loginAs(projectRepository, project.getId(), String.valueOf(admin.getId()), "ROLE_ADMIN");
    }

    /**
     * 템플릿에서 용어 시트(0)만 시험용 행으로 바꾼다. 다른 열(도메인명/허용값 등)은 템플릿의 같은 약어 행을 복사해 쓴다.
     * (템플릿에 없는 약어면 GIS_DATA_YN 행의 나머지 열을 쓴다)
     */
    private static MockMultipartFile templateWithTerms(List<TermRow> rows) throws IOException {
        try (InputStream in = TermUploadNameGenerationTest.class.getClassLoader().getResourceAsStream("static/excel/dataPortal.xlsx");
             Workbook workbook = new XSSFWorkbook(in)) {
            Sheet terms = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            // 약어 → 템플릿 행 값 (도메인명 등 나머지 열 복사용)
            Map<String, List<String>> templateByAbbreviation = new java.util.HashMap<>();
            for (int r = 1; r <= terms.getLastRowNum(); r++) {
                Row row = terms.getRow(r);
                if (row == null) continue;
                List<String> values = new java.util.ArrayList<>();
                for (int c = 0; c < 12; c++) values.add(formatter.formatCellValue(row.getCell(c)));
                templateByAbbreviation.putIfAbsent(values.get(COL_ABBREVIATION), values);
            }

            for (int r = terms.getLastRowNum(); r >= 1; r--) {
                Row row = terms.getRow(r);
                if (row != null) terms.removeRow(row);
            }

            for (int i = 0; i < rows.size(); i++) {
                TermRow termRow = rows.get(i);
                List<String> source = templateByAbbreviation.getOrDefault(termRow.abbreviation(), templateByAbbreviation.get("GIS_DATA_YN"));
                assertThat(source).as("템플릿에 GIS_DATA_YN 행이 있어야 한다").isNotNull();

                Row row = terms.createRow(i + 1);
                for (int c = 0; c < source.size(); c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(source.get(c));
                }
                row.getCell(COL_NAME).setCellValue(termRow.name());
                row.getCell(COL_DESCRIPTION).setCellValue(termRow.description());
                row.getCell(COL_ABBREVIATION).setCellValue(termRow.abbreviation());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new MockMultipartFile("file", "dataPortal-term-test.xlsx", null, out.toByteArray());
        }
    }
}
