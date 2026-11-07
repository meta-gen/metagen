package com.koboolean.metagen;

import com.koboolean.metagen.common.utils.ExcelUtils;
import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.security.repository.AccountRepository;
import com.koboolean.metagen.core.support.SecurityTestSupport;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
import com.koboolean.metagen.data.dictionary.service.DataDictionaryService;
import com.koboolean.metagen.data.dictionary.service.StandardTermService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 공통표준용어 템플릿(static/excel/dataPortal.xlsx)
 * - 표준용어명은 영문약어명과 같은 수의 단어를 '_' 로 잇는다. (GIS_자료_여부 / GIS_DATA_YN)
 * - 템플릿 그대로 데이터사전 업로드가 끝까지 성공해야 한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class DataPortalTemplateTest {

    @Autowired DataDictionaryService dataDictionaryService;
    @Autowired StandardTermRepository standardTermRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired AccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        SecurityTestSupport.clear();
    }

    private static MockMultipartFile template() throws IOException {
        try (InputStream in = DataPortalTemplateTest.class.getClassLoader().getResourceAsStream("static/excel/dataPortal.xlsx")) {
            assertThat(in).as("dataPortal.xlsx").isNotNull();
            return new MockMultipartFile("file", "dataPortal.xlsx", null, in.readAllBytes());
        }
    }

    @Test
    @DisplayName("모든 표준용어의 용어명과 영문약어명은 '_' 기준 단어 수가 같다")
    void termNamesMatchAbbreviations() throws IOException {
        List<String> rows = new ArrayList<>();
        ExcelUtils.readExcelRows(template(), 0, List.of("id", "revision", "name", "description", "abbreviation"), 1, row -> {
            if (row.get("name").isEmpty()) return;
            rows.add(row.get("name"));
            assertThatCode(() -> StandardTermService.validateWordCount(row.get("name"), row.get("abbreviation")))
                    .as(row.get("name") + " / " + row.get("abbreviation"))
                    .doesNotThrowAnyException();
        });

        assertThat(rows).hasSizeGreaterThan(1_000).contains("GIS_자료_여부", "HTML_사용_여부");
    }

    @Test
    @DisplayName("템플릿 그대로 데이터사전 업로드(도메인 → 단어 → 용어)가 성공한다")
    void uploadTemplate() throws IOException {
        // 다른 테스트 데이터와 섞이지 않도록 새 프로젝트에 올린다
        Account admin = accountRepository.findAll().stream()
                .filter(account -> "admin".equals(account.getUsername())).findFirst().orElseThrow();
        Project project = projectRepository.save(Project.builder()
                .projectName("dataPortal-template-test").isActive(true).account(admin).build());
        AccountDto login = SecurityTestSupport.loginAs(projectRepository, project.getId(), String.valueOf(admin.getId()), "ROLE_ADMIN");

        long before = standardTermRepository.count();
        dataDictionaryService.uploadDictionaryExcelFile(template(), login);

        List<Map<String, String>> terms = new ArrayList<>();
        ExcelUtils.readExcelRows(template(), 0, List.of("id", "revision", "name"), 1, row -> {
            if (!row.get("name").isEmpty()) terms.add(row);
        });
        assertThat(standardTermRepository.count() - before).isEqualTo(terms.size());
    }
}
