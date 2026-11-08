package com.koboolean.metagen.data.column.service.impl;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.support.SecurityTestSupport;
import com.koboolean.metagen.data.column.domain.dto.ColumnInfoDto;
import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import com.koboolean.metagen.data.column.repository.ColumnInfoRepository;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
import com.koboolean.metagen.data.table.domain.entity.TableInfo;
import com.koboolean.metagen.data.table.repository.TableInfoRepository;
import jakarta.persistence.EntityManager;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColumnManageServiceImplTest {

    @Mock
    ColumnInfoRepository columnInfoRepository;
    @Mock
    TableInfoRepository tableInfoRepository;
    @Mock
    StandardTermRepository standardTermRepository;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    ColumnManageServiceImpl service;

    @AfterEach
    void tearDown() {
        SecurityTestSupport.clear();
    }

    private static TableInfo table() {
        return TableInfo.builder().id(10L).projectId(1L).tableName("TB_USER").columns(new ArrayList<>()).build();
    }

    private static StandardTerm term() {
        return StandardTerm.builder().id(20L).projectId(1L).commonStandardTermName("사용자_이름").columnInfos(new ArrayList<>()).build();
    }

    private static ColumnInfoDto newColumn() {
        return ColumnInfoDto.builder()
                .tableInfoId(10L).termId(20L).tableName("TB_USER").columnName("USER_NM").columnDesc("사용자 이름")
                .dataType("VARCHAR").maxLength(100).sortOrder(1)
                .isPk("N").isRequired("N").isUnique("N").isNullable("Y")
                .build();
    }

    private static ColumnInfo column(Long id, boolean approved) {
        return ColumnInfo.builder().id(id).projectId(1L).columnName("USER_NM").isApproval(approved).sortOrder(1).build();
    }

    @Nested
    @DisplayName("컬럼 등록")
    class Insert {

        @Test
        @DisplayName("같은 테이블에 같은 컬럼명이 있으면 SAVED_DATA_EXISTS")
        void duplicate() {
            AccountDto login = SecurityTestSupport.loginAsAdmin(1L);
            when(tableInfoRepository.findByIdAndProjectId(10L, 1L)).thenReturn(table());
            when(standardTermRepository.findByIdAndProjectId(20L, 1L)).thenReturn(term());
            when(columnInfoRepository.findAllByProjectIdAndTableInfo_IdAndColumnName(1L, 10L, "USER_NM")).thenReturn(List.of(column(1L, true)));

            assertThatThrownBy(() -> service.insertColumn(login, newColumn()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.SAVED_DATA_EXISTS);
            verify(columnInfoRepository, never()).save(any());
        }

        @Test
        @DisplayName("다른 테이블에 같은 컬럼명이 있어도 등록할 수 있다 (중복은 같은 테이블 안에서만 확인)")
        void sameNameInOtherTable() {
            AccountDto login = SecurityTestSupport.loginAsAdmin(1L);
            TableInfo table = table();
            when(tableInfoRepository.findByIdAndProjectId(10L, 1L)).thenReturn(table);
            when(standardTermRepository.findByIdAndProjectId(20L, 1L)).thenReturn(term());
            when(columnInfoRepository.findAllByProjectIdAndTableInfo_IdAndColumnName(1L, 10L, "USER_NM")).thenReturn(List.of());

            service.insertColumn(login, newColumn());

            verify(columnInfoRepository).save(any());
            assertThat(table.getColumns()).hasSize(1);
        }

        @Test
        @DisplayName("테이블을 찾을 수 없으면 TABLE_IS_NOT_DEFINED (저장하지 않음)")
        void tableMissing() {
            AccountDto login = SecurityTestSupport.loginAsAdmin(1L);
            when(tableInfoRepository.findByIdAndProjectId(10L, 1L)).thenReturn(null);

            assertThatThrownBy(() -> service.insertColumn(login, newColumn()))
                    .extracting("errorCode").isEqualTo(ErrorCode.TABLE_IS_NOT_DEFINED);
            verify(columnInfoRepository, never()).save(any());
        }

        @Test
        @DisplayName("표준용어를 찾을 수 없으면 NOT_FOUND_TERM_DATA (저장하지 않음)")
        void termMissing() {
            AccountDto login = SecurityTestSupport.loginAsAdmin(1L);
            when(tableInfoRepository.findByIdAndProjectId(10L, 1L)).thenReturn(table());
            when(standardTermRepository.findByIdAndProjectId(20L, 1L)).thenReturn(null);

            assertThatThrownBy(() -> service.insertColumn(login, newColumn()))
                    .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND_TERM_DATA);
            verify(columnInfoRepository, never()).save(any());
        }

        @Test
        @DisplayName("테이블/표준용어와 양방향 연관을 맺고, 일반 사용자 등록은 미승인으로 저장한다")
        void userInsert() {
            AccountDto login = SecurityTestSupport.loginAsUser(1L);
            TableInfo table = table();
            StandardTerm term = term();
            when(tableInfoRepository.findByIdAndProjectId(10L, 1L)).thenReturn(table);
            when(standardTermRepository.findByIdAndProjectId(20L, 1L)).thenReturn(term);

            service.insertColumn(login, newColumn());

            ArgumentCaptor<ColumnInfo> saved = ArgumentCaptor.forClass(ColumnInfo.class);
            verify(columnInfoRepository).save(saved.capture());
            ColumnInfo column = saved.getValue();
            assertThat(column.getProjectId()).isEqualTo(1L);
            assertThat(column.getIsApproval()).isFalse();
            assertThat(column.getTableInfo()).isSameAs(table);
            assertThat(column.getStandardTerms()).isSameAs(term);
            assertThat(table.getColumns()).containsExactly(column);
            assertThat(term.getColumnInfos()).containsExactly(column);
        }

        @Test
        @DisplayName("승인 권한자가 등록하면 승인으로 저장하고, PK 컬럼은 필수/유일로 설정한다")
        void approverInsertPk() {
            AccountDto login = SecurityTestSupport.loginAsAdmin(1L);
            when(tableInfoRepository.findByIdAndProjectId(10L, 1L)).thenReturn(table());
            when(standardTermRepository.findByIdAndProjectId(20L, 1L)).thenReturn(term());
            ColumnInfoDto dto = newColumn();
            dto.setIsPk("Y");

            service.insertColumn(login, dto);

            ArgumentCaptor<ColumnInfo> saved = ArgumentCaptor.forClass(ColumnInfo.class);
            verify(columnInfoRepository).save(saved.capture());
            assertThat(saved.getValue().getIsApproval()).isTrue();
            assertThat(saved.getValue().getIsPk()).isTrue();
            assertThat(saved.getValue().getIsRequired()).isTrue();
            assertThat(saved.getValue().getIsUnique()).isTrue();
        }
    }

    @Test
    @DisplayName("승인된 컬럼은 삭제할 수 없다")
    void deleteApproved() {
        when(columnInfoRepository.findByProjectIdAndId(1L, 1L)).thenReturn(column(1L, true));

        assertThatThrownBy(() -> service.deleteColumn(AccountDto.builder().projectId(1L).build(),
                List.of(ColumnInfoDto.builder().id(1L).build())))
                .extracting("errorCode").isEqualTo(ErrorCode.APPROVED_DATA_CANNOT_BE_DELETED);
        verify(columnInfoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("미승인 컬럼 삭제 시 테이블/표준용어 연관에서도 제거한다 (테이블은 이름이 아닌 연관관계로 찾는다)")
    void deleteColumn() {
        ColumnInfo column = column(1L, false);
        TableInfo table = table();
        table.getColumns().add(column);
        column.setTableInfo(table);
        StandardTerm term = term();
        term.getColumnInfos().add(column);
        when(columnInfoRepository.findByProjectIdAndId(1L, 1L)).thenReturn(column);
        when(standardTermRepository.findByIdAndProjectId(20L, 1L)).thenReturn(term);

        // 컬럼에 예전 테이블명이 남아있어도 삭제할 수 있어야 한다.
        service.deleteColumn(AccountDto.builder().projectId(1L).build(),
                List.of(ColumnInfoDto.builder().id(1L).tableName("TB_OLD_NAME").termId(20L).build()));

        verify(columnInfoRepository).delete(column);
        assertThat(table.getColumns()).isEmpty();
        assertThat(term.getColumnInfos()).isEmpty();
    }

    @Test
    @DisplayName("승인된 컬럼은 정렬순서를 바꿀 수 없다")
    void sortOrderApproved() {
        when(columnInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(column(1L, true));

        assertThatThrownBy(() -> service.updateSortOrder(AccountDto.builder().projectId(1L).build(),
                List.of(ColumnInfoDto.builder().id(1L).sortOrder(5).build())))
                .extracting("errorCode").isEqualTo(ErrorCode.APPROVED_DATA_CANNOT_BE_UPDATE);
    }

    @Test
    @DisplayName("미승인 컬럼의 정렬순서를 변경한다")
    void sortOrder() {
        ColumnInfo column = column(1L, false);
        when(columnInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(column);

        service.updateSortOrder(AccountDto.builder().projectId(1L).build(), List.of(ColumnInfoDto.builder().id(1L).sortOrder(5).build()));

        assertThat(column.getSortOrder()).isEqualTo(5);
    }

    @Test
    @DisplayName("승인 상태 변경은 type 문자열(true/false)을 그대로 반영한다")
    void approval() {
        ColumnInfo column = column(1L, false);
        when(columnInfoRepository.findByProjectIdAndId(1L, 1L)).thenReturn(column);

        service.updateColumnApproval(AccountDto.builder().projectId(1L).build(), List.of(ColumnInfoDto.builder().id(1L).build()), "true");

        assertThat(column.getIsApproval()).isTrue();
    }

    @Test
    @DisplayName("컬럼명 중복 확인 : 같은 테이블에 같은 컬럼명이 있으면 그 컬럼을 돌려준다")
    void duplicateColumns() {
        ColumnInfo existing = column(1L, true);
        existing.setTableName("TB_USER");
        when(columnInfoRepository.findAllByProjectIdAndTableInfo_IdAndColumnName(1L, 10L, "USER_NM")).thenReturn(List.of(existing));
        when(columnInfoRepository.findAllByProjectIdAndTableInfo_IdAndColumnName(1L, 11L, "USER_NM")).thenReturn(List.of());

        AccountDto login = AccountDto.builder().projectId(1L).build();

        assertThat(service.selectDuplicateColumns(login, 10L, "USER_NM"))
                .singleElement().satisfies(dto -> assertThat(dto.getTableName()).isEqualTo("TB_USER"));
        assertThat(service.selectDuplicateColumns(login, 11L, "USER_NM")).as("다른 테이블은 중복 아님").isEmpty();
    }

    @Nested
    @DisplayName("컬럼 엑셀 업로드 : 같은 테이블 안에서만 컬럼명 중복을 막는다")
    class Upload {

        private final AccountDto login = AccountDto.builder().projectId(1L).build();

        /**
         * 컬럼 템플릿 형식(헤더 1행 + 데이터)의 엑셀을 만든다. 각 행은 {테이블명, 컬럼명}
         */
        private MockMultipartFile excel(String[]... rows) throws IOException {
            try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet();
                sheet.createRow(0).createCell(0).setCellValue("테이블명");

                for (int i = 0; i < rows.length; i++) {
                    Row row = sheet.createRow(i + 1);
                    // tableName, columnName, columnDesc, dataType, maxLength, precision, scale, isNullable, defaultValue, sortOrder,
                    // isMasterData, refTableName, isRequired, isSensitive, isPk, isUnique, isIndex, isEncrypted, example
                    String[] values = {rows[i][0], rows[i][1], "설명", "VARCHAR", "100", "0", "0", "Y", "", "1",
                            "N", "", "N", "N", "N", "N", "N", "N", ""};
                    for (int j = 0; j < values.length; j++) {
                        row.createCell(j).setCellValue(values[j]);
                    }
                }

                workbook.write(out);
                return new MockMultipartFile("file", "column.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
            }
        }

        private StandardTerm userNameTerm() {
            StandardTerm term = term();
            term.setCommonStandardTermAbbreviation("USER_NM");
            return term;
        }

        /** 청크 단위 IN 조회 : 테이블명 / 영문약어 */
        private void stubTablesAndTerm(TableInfo... tables) {
            when(tableInfoRepository.findAllByProjectIdAndTableNameIn(eq(1L), anyCollection())).thenReturn(List.of(tables));
            when(standardTermRepository.findAllByProjectIdAndCommonStandardTermAbbreviationIn(eq(1L), anyCollection()))
                    .thenReturn(List.of(userNameTerm()));
        }

        @Test
        @DisplayName("파일 안에 같은 테이블의 같은 컬럼명이 두 번 있으면 COLUMN_DUPLICATE_IN_FILE")
        void duplicateInFile() throws IOException {
            SecurityTestSupport.loginAsAdmin(1L);
            stubTablesAndTerm(table());

            assertThatThrownBy(() -> service.uploadColumnExcelFile(excel(
                    new String[]{"TB_USER", "USER_NM"},
                    new String[]{"tb_user", "USER_NM"}), login))
                    .extracting("errorCode").isEqualTo(ErrorCode.COLUMN_DUPLICATE_IN_FILE);
            verify(columnInfoRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("같은 테이블에 이미 등록된 컬럼명이면 SAVED_DATA_EXISTS (저장하지 않음)")
        void duplicateInDb() throws IOException {
            SecurityTestSupport.loginAsAdmin(1L);
            stubTablesAndTerm(table());
            when(columnInfoRepository.findTableColumnKeys(eq(1L), anyCollection(), anyCollection()))
                    .thenReturn(List.<Object[]>of(new Object[]{10L, "USER_NM"}));

            assertThatThrownBy(() -> service.uploadColumnExcelFile(excel(new String[]{"TB_USER", "USER_NM"}), login))
                    .extracting("errorCode").isEqualTo(ErrorCode.SAVED_DATA_EXISTS);
            verify(columnInfoRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("없는 테이블이면 TABLE_IS_NOT_DEFINED, 없는 표준용어면 TERM_IS_NOT_DEFINED")
        void missingReferences() {
            SecurityTestSupport.loginAsAdmin(1L);
            when(tableInfoRepository.findAllByProjectIdAndTableNameIn(eq(1L), anyCollection())).thenReturn(List.of());

            assertThatThrownBy(() -> service.uploadColumnExcelFile(excel(new String[]{"TB_NONE", "USER_NM"}), login))
                    .extracting("errorCode").isEqualTo(ErrorCode.TABLE_IS_NOT_DEFINED);

            when(tableInfoRepository.findAllByProjectIdAndTableNameIn(eq(1L), anyCollection())).thenReturn(List.of(table()));

            assertThatThrownBy(() -> service.uploadColumnExcelFile(excel(new String[]{"TB_USER", "NO_TERM"}), login))
                    .extracting("errorCode").isEqualTo(ErrorCode.TERM_IS_NOT_DEFINED);
        }

        @Test
        @DisplayName("다른 테이블이면 같은 컬럼명도 함께 업로드되고, 한 청크를 배치로 저장한 뒤 영속성 컨텍스트를 비운다")
        @SuppressWarnings("unchecked")
        void sameNameInOtherTables() throws IOException {
            SecurityTestSupport.loginAsAdmin(1L);
            TableInfo order = TableInfo.builder().id(11L).projectId(1L).tableName("TB_ORDER").columns(new ArrayList<>()).build();
            stubTablesAndTerm(table(), order);

            service.uploadColumnExcelFile(excel(
                    new String[]{"TB_USER", "USER_NM"},
                    new String[]{"TB_ORDER", "USER_NM"}), login);

            ArgumentCaptor<List<ColumnInfo>> saved = ArgumentCaptor.forClass(List.class);
            verify(columnInfoRepository).saveAll(saved.capture());
            assertThat(saved.getValue()).extracting(ColumnInfo::getTableName, ColumnInfo::getColumnName)
                    .containsExactly(tuple("TB_USER", "USER_NM"), tuple("TB_ORDER", "USER_NM"));

            // 테이블/용어는 조회 없이 ID 참조로 연결한다
            verify(entityManager).getReference(TableInfo.class, 10L);
            verify(entityManager).getReference(TableInfo.class, 11L);
            verify(entityManager).flush();
            verify(entityManager).clear();
            // 테이블의 컬럼 목록 컬렉션은 건드리지 않는다 (FK 기준)
            assertThat(order.getColumns()).isEmpty();
        }
    }
}
