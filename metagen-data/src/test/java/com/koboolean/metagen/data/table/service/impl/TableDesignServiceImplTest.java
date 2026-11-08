package com.koboolean.metagen.data.table.service.impl;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.support.SecurityTestSupport;
import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import com.koboolean.metagen.data.column.repository.ColumnInfoRepository;
import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardWordRepository;
import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
import com.koboolean.metagen.data.table.domain.dto.TableNameDto;
import com.koboolean.metagen.data.table.domain.entity.TableInfo;
import com.koboolean.metagen.data.table.repository.TableInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableDesignServiceImplTest {

    @Mock
    TableInfoRepository tableInfoRepository;

    @Mock
    ColumnInfoRepository columnInfoRepository;

    @Mock
    StandardWordRepository standardWordRepository;

    @Mock
    ProjectRepository projectRepository; // 기본 : findById → Optional.empty (접두어 없음)

    @InjectMocks
    TableDesignServiceImpl service;

    @AfterEach
    void tearDown() {
        SecurityTestSupport.clear();
    }

    private static TableInfo table(Long id, String name, boolean approved) {
        return TableInfo.builder().id(id).projectId(1L).tableName(name).isApproval(approved).columns(new ArrayList<>()).build();
    }

    private static TableInfoDto newTable(String name) {
        return TableInfoDto.builder().tableName(name).tableDescription("설명").isMasterTable("N").sortOrder(1).build();
    }

    /** 신규 등록용 : 한글명으로만 테이블명을 만든다 */
    private static TableInfoDto newKorTable(String korName, String description) {
        return TableInfoDto.builder().tableKorName(korName).tableDescription(description).isMasterTable("N").sortOrder(1).build();
    }

    private static StandardWord word(String name, String abbreviation, boolean approved) {
        return StandardWord.builder().projectId(1L).commonStandardWordName(name)
                .commonStandardWordAbbreviation(abbreviation).commonStandardWordEnglishName(name + "-en")
                .isApproval(approved).build();
    }

    /** 사용(USE) / 내역(HIST) / 관리(MGMT) 를 승인된 표준 단어로 등록 */
    private void stubApprovedWords() {
        when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(1L, "사용")).thenReturn(List.of(word("사용", "USE", true)));
        when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(1L, "내역")).thenReturn(List.of(word("내역", "hist", true)));
        when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(1L, "관리")).thenReturn(List.of(word("관리", "MGMT", true)));
    }

    @Nested
    @DisplayName("표준 단어 기준 테이블명 생성")
    class Generate {

        private final AccountDto login = AccountDto.builder().projectId(1L).build();

        @Test
        @DisplayName("사용_내역_관리 → 단어별 영문약어를 대문자로 이어 USE_HIST_MGMT, 설명은 '사용 내역 관리'")
        void generate() {
            stubApprovedWords();

            TableNameDto result = service.generateTableName("사용_내역_관리", login);

            assertThat(result.valid()).isTrue();
            assertThat(result.tableName()).isEqualTo("USE_HIST_MGMT");
            assertThat(result.tableKorName()).isEqualTo("사용 내역 관리");
            assertThat(result.tablePrefix()).isNull();
            assertThat(result.words()).extracting(TableNameDto.Word::status).containsOnly(TableNameDto.Status.APPROVED);
        }

        @Test
        @DisplayName("프로젝트에 접두어(TB_)가 설정되어 있으면 테이블명 앞에 붙인다")
        void prefix() {
            stubApprovedWords();
            when(projectRepository.findById(1L)).thenReturn(Optional.of(Project.builder().id(1L).tablePrefix("TB_").build()));

            TableNameDto result = service.generateTableName("사용_내역_관리", login);

            assertThat(result.tableName()).isEqualTo("TB_USE_HIST_MGMT");
            assertThat(result.tablePrefix()).isEqualTo("TB_");
        }

        @Test
        @DisplayName("공백이나 연속된 구분자로 입력해도 같은 결과를 만든다")
        void separators() {
            stubApprovedWords();

            assertThat(service.generateTableName("  사용 내역__관리 ", login).tableName()).isEqualTo("USE_HIST_MGMT");
        }

        @Test
        @DisplayName("미승인/미등록 단어가 있으면 테이블명을 만들지 않고 단어별 상태를 알려준다")
        void invalidWords() {
            when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(1L, "사용")).thenReturn(List.of(word("사용", "USE", true)));
            when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(1L, "이력")).thenReturn(List.of(word("이력", "HIST", false)));
            when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(1L, "통계")).thenReturn(List.of());

            TableNameDto result = service.generateTableName("사용_이력_통계", login);

            assertThat(result.valid()).isFalse();
            assertThat(result.tableName()).isNull();
            assertThat(result.words()).extracting(TableNameDto.Word::status).containsExactly(
                    TableNameDto.Status.APPROVED, TableNameDto.Status.NOT_APPROVED, TableNameDto.Status.NOT_FOUND);
            assertThat(result.invalidWords()).containsExactly("이력", "통계");
        }

        @Test
        @DisplayName("같은 이름의 단어가 여러 건이면 승인된 단어를 사용한다")
        void preferApproved() {
            when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(1L, "내역"))
                    .thenReturn(List.of(word("내역", "DTLS", false), word("내역", "HIST", true)));

            assertThat(service.generateTableName("내역", login).tableName()).isEqualTo("HIST");
        }

        @Test
        @DisplayName("빈 값이면 유효하지 않다")
        void empty() {
            assertThat(service.generateTableName(" _ ", login).valid()).isFalse();
        }
    }

    @Nested
    @DisplayName("테이블 등록")
    class Save {

        @Test
        @DisplayName("한글명이 없으면 TABLE_KOR_NAME_REQUIRED")
        void korNameRequired() {
            AccountDto login = SecurityTestSupport.loginAsAdmin(1L);

            assertThatThrownBy(() -> service.saveTable(newTable("TB_USER"), login))
                    .extracting("errorCode").isEqualTo(ErrorCode.TABLE_KOR_NAME_REQUIRED);
            verify(tableInfoRepository, never()).save(any());
        }

        @Test
        @DisplayName("승인된 표준 단어가 아닌 단어가 있으면 TABLE_WORD_NOT_STANDARD")
        void notStandard() {
            AccountDto login = SecurityTestSupport.loginAsAdmin(1L);
            when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(1L, "없는단어")).thenReturn(List.of());

            assertThatThrownBy(() -> service.saveTable(newKorTable("없는단어", "설명"), login))
                    .extracting("errorCode").isEqualTo(ErrorCode.TABLE_WORD_NOT_STANDARD);
            verify(tableInfoRepository, never()).save(any());
        }

        @Test
        @DisplayName("생성된 테이블명(대문자)이 같은 프로젝트에 있으면 SAVED_DATA_EXISTS")
        void duplicate() {
            AccountDto login = SecurityTestSupport.loginAsAdmin(1L);
            stubApprovedWords();
            when(tableInfoRepository.findAllByTableNameAndProjectId("USE_HIST_MGMT", 1L)).thenReturn(List.of(table(1L, "USE_HIST_MGMT", true)));

            assertThatThrownBy(() -> service.saveTable(newKorTable("사용_내역_관리", "설명"), login))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.SAVED_DATA_EXISTS);
            verify(tableInfoRepository, never()).save(any());
        }

        @Test
        @DisplayName("승인 권한자가 등록하면 생성된 테이블명으로 승인 저장하고, 설명이 비어있으면 한글명으로 채운다")
        void approver() {
            AccountDto login = SecurityTestSupport.loginAsAdmin(1L);
            stubApprovedWords();

            service.saveTable(newKorTable("사용_내역_관리", ""), login);

            ArgumentCaptor<TableInfo> saved = ArgumentCaptor.forClass(TableInfo.class);
            verify(tableInfoRepository).save(saved.capture());
            assertThat(saved.getValue().getTableName()).isEqualTo("USE_HIST_MGMT");
            assertThat(saved.getValue().getTableKorName()).isEqualTo("사용 내역 관리");
            assertThat(saved.getValue().getTableDescription()).isEqualTo("사용 내역 관리");
            assertThat(saved.getValue().getProjectId()).isEqualTo(1L);
            assertThat(saved.getValue().getIsApproval()).isTrue();
        }

        @Test
        @DisplayName("일반 사용자가 등록하면 미승인으로 저장하고, 입력한 설명은 유지한다")
        void user() {
            AccountDto login = SecurityTestSupport.loginAsUser(1L);
            stubApprovedWords();

            service.saveTable(newKorTable("사용_내역_관리", "서비스 사용 내역"), login);

            ArgumentCaptor<TableInfo> saved = ArgumentCaptor.forClass(TableInfo.class);
            verify(tableInfoRepository).save(saved.capture());
            assertThat(saved.getValue().getIsApproval()).isFalse();
            assertThat(saved.getValue().getTableDescription()).isEqualTo("서비스 사용 내역");
        }
    }

    @Test
    @DisplayName("수정 시 한글명을 입력하면 표준 단어 기준으로 테이블명을 다시 만든다")
    void updateWithKorName() {
        AccountDto login = AccountDto.builder().projectId(1L).build();
        TableInfo table = table(1L, "TB_USER", false);
        TableInfoDto dto = TableInfoDto.builder().id(1L).tableName("TB_USER").tableKorName("사용_내역_관리")
                .tableDescription("사용 내역").isMasterTable("N").sortOrder(1).build();
        stubApprovedWords();
        when(tableInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(table);
        when(tableInfoRepository.findAllByTableNameAndProjectId("USE_HIST_MGMT", 1L)).thenReturn(List.of());

        service.updateTable(dto, login);

        assertThat(table.getTableName()).isEqualTo("USE_HIST_MGMT");
        assertThat(table.getTableKorName()).isEqualTo("사용 내역 관리");
    }

    @Test
    @DisplayName("수정 시 한글명을 보내지 않으면 저장된 한글명을 유지한다")
    void updateKeepsKorName() {
        AccountDto login = AccountDto.builder().projectId(1L).build();
        TableInfo table = table(1L, "USE_HIST_MGMT", false);
        table.setTableKorName("사용 내역 관리");
        TableInfoDto dto = TableInfoDto.builder().id(1L).tableName("USE_HIST_MGMT").tableKorName("")
                .tableDescription("설명 변경").isMasterTable("N").sortOrder(1).build();
        when(tableInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(table);
        when(tableInfoRepository.findAllByTableNameAndProjectId("USE_HIST_MGMT", 1L)).thenReturn(List.of(table));

        service.updateTable(dto, login);

        assertThat(table.getTableKorName()).isEqualTo("사용 내역 관리");
        assertThat(table.getTableDescription()).isEqualTo("설명 변경");
    }

    @Test
    @DisplayName("수정 시 테이블명을 바꾸지 않으면 자기 자신은 중복으로 보지 않는다")
    void updateSameName() {
        AccountDto login = AccountDto.builder().projectId(1L).build();
        TableInfo table = table(1L, "TB_USER", false);
        TableInfoDto dto = TableInfoDto.builder().id(1L).tableName("TB_USER").tableDescription("사용자").isMasterTable("N").sortOrder(2).build();
        when(tableInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(table);
        when(tableInfoRepository.findAllByTableNameAndProjectId("TB_USER", 1L)).thenReturn(List.of(table));

        service.updateTable(dto, login);

        assertThat(table.getTableDescription()).isEqualTo("사용자");
        assertThat(table.getSortOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("테이블명을 바꾸면 소속 컬럼의 테이블명과 다른 컬럼의 참조 테이블명도 함께 바뀐다")
    void updateRenamesColumns() {
        AccountDto login = AccountDto.builder().projectId(1L).build();
        TableInfo table = table(1L, "TB_USER", false);
        ColumnInfo own = ColumnInfo.builder().id(10L).tableName("TB_USER").build();
        table.getColumns().add(own);
        ColumnInfo referencing = ColumnInfo.builder().id(20L).tableName("TB_ORDER").refTableName("TB_USER").build();
        TableInfoDto dto = TableInfoDto.builder().id(1L).tableName("tb_member").tableDescription("회원").isMasterTable("N").sortOrder(1).build();
        when(tableInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(table);
        when(tableInfoRepository.findAllByTableNameAndProjectId("TB_MEMBER", 1L)).thenReturn(List.of());
        when(columnInfoRepository.findAllByProjectIdAndRefTableName(1L, "TB_USER")).thenReturn(List.of(referencing));

        service.updateTable(dto, login);

        assertThat(own.getTableName()).isEqualTo("TB_MEMBER");
        assertThat(referencing.getRefTableName()).isEqualTo("TB_MEMBER");
        assertThat(referencing.getTableName()).isEqualTo("TB_ORDER");
    }

    @Test
    @DisplayName("테이블명을 바꾸지 않으면 컬럼은 건드리지 않는다")
    void updateKeepsColumns() {
        AccountDto login = AccountDto.builder().projectId(1L).build();
        TableInfo table = table(1L, "TB_USER", false);
        TableInfoDto dto = TableInfoDto.builder().id(1L).tableName("TB_USER").tableDescription("사용자").isMasterTable("N").sortOrder(1).build();
        when(tableInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(table);
        when(tableInfoRepository.findAllByTableNameAndProjectId("TB_USER", 1L)).thenReturn(List.of(table));

        service.updateTable(dto, login);

        verify(columnInfoRepository, never()).findAllByProjectIdAndRefTableName(any(), any());
    }

    @Test
    @DisplayName("수정 시 테이블명을 대문자로 바꾸고 설명/마스터 여부/정렬순서를 반영한다")
    void update() {
        AccountDto login = AccountDto.builder().projectId(1L).build();
        TableInfo table = table(1L, "TB_USER", false);
        TableInfoDto dto = TableInfoDto.builder().id(1L).tableName("tb_member").tableDescription("회원").isMasterTable("Y").sortOrder(3).build();
        when(tableInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(table);
        when(tableInfoRepository.findAllByTableNameAndProjectId("TB_MEMBER", 1L)).thenReturn(List.of());

        service.updateTable(dto, login);

        assertThat(table.getTableName()).isEqualTo("TB_MEMBER");
        assertThat(table.getTableDescription()).isEqualTo("회원");
        assertThat(table.getIsMasterTable()).isTrue();
        assertThat(table.getSortOrder()).isEqualTo(3);
    }

    @Nested
    @DisplayName("테이블 삭제")
    class Delete {

        private final AccountDto login = AccountDto.builder().projectId(1L).build();

        @Test
        @DisplayName("컬럼이 등록된 테이블은 삭제할 수 없다")
        void hasColumns() {
            TableInfo table = table(1L, "TB_USER", false);
            table.getColumns().add(new ColumnInfo());
            when(tableInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(table);

            assertThatThrownBy(() -> service.deleteTable(List.of(TableInfoDto.builder().id(1L).build()), login))
                    .extracting("errorCode").isEqualTo(ErrorCode.COLUMN_DEPENDENCY_EXISTS);
        }

        @Test
        @DisplayName("승인된 테이블은 삭제할 수 없다")
        void approved() {
            when(tableInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(table(1L, "TB_USER", true));

            assertThatThrownBy(() -> service.deleteTable(List.of(TableInfoDto.builder().id(1L).build()), login))
                    .extracting("errorCode").isEqualTo(ErrorCode.APPROVED_DATA_CANNOT_BE_DELETED);
            verify(tableInfoRepository, never()).delete(any());
        }

        @Test
        @DisplayName("컬럼 없는 미승인 테이블은 삭제한다")
        void delete() {
            TableInfo table = table(1L, "TB_USER", false);
            when(tableInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(table);

            service.deleteTable(List.of(TableInfoDto.builder().id(1L).build()), login);

            verify(tableInfoRepository).delete(table);
        }
    }

    @Test
    @DisplayName("승인 상태 일괄 변경")
    void approval() {
        TableInfo table = table(1L, "TB_USER", false);
        when(tableInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(table);

        service.updateTableIsApproval(true, AccountDto.builder().projectId(1L).build(), List.of(TableInfoDto.builder().id(1L).build()));

        assertThat(table.getIsApproval()).isTrue();
    }
}
