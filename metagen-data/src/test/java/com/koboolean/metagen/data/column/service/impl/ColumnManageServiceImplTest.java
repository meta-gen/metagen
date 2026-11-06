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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
        return StandardTerm.builder().id(20L).projectId(1L).commonStandardTermName("사용자 이름").columnInfos(new ArrayList<>()).build();
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
        @DisplayName("같은 프로젝트에 같은 컬럼명이 있으면 SAVED_DATA_EXISTS")
        void duplicate() {
            AccountDto login = SecurityTestSupport.loginAsAdmin(1L);
            when(columnInfoRepository.findAllByProjectIdAndColumnName(1L, "USER_NM")).thenReturn(List.of(column(1L, true)));

            assertThatThrownBy(() -> service.insertColumn(login, newColumn()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.SAVED_DATA_EXISTS);
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
    @DisplayName("미승인 컬럼 삭제 시 테이블/표준용어 연관에서도 제거한다")
    void deleteColumn() {
        ColumnInfo column = column(1L, false);
        TableInfo table = table();
        table.getColumns().add(column);
        StandardTerm term = term();
        term.getColumnInfos().add(column);
        when(columnInfoRepository.findByProjectIdAndId(1L, 1L)).thenReturn(column);
        when(tableInfoRepository.findByTableNameAndProjectId("TB_USER", 1L)).thenReturn(table);
        when(standardTermRepository.findByIdAndProjectId(20L, 1L)).thenReturn(term);

        service.deleteColumn(AccountDto.builder().projectId(1L).build(),
                List.of(ColumnInfoDto.builder().id(1L).tableName("TB_USER").termId(20L).build()));

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
}
