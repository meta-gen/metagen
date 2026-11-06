package com.koboolean.metagen.data.table.service.impl;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.support.SecurityTestSupport;
import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import com.koboolean.metagen.data.table.domain.dto.TableInfoDto;
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
class TableDesignServiceImplTest {

    @Mock
    TableInfoRepository tableInfoRepository;

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

    @Nested
    @DisplayName("테이블 등록")
    class Save {

        @Test
        @DisplayName("같은 프로젝트에 같은 테이블명이 있으면 SAVED_DATA_EXISTS")
        void duplicate() {
            AccountDto login = SecurityTestSupport.loginAsAdmin(1L);
            when(tableInfoRepository.findAllByTableNameAndProjectId("TB_USER", 1L)).thenReturn(List.of(table(1L, "TB_USER", true)));

            assertThatThrownBy(() -> service.saveTable(newTable("TB_USER"), login))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.SAVED_DATA_EXISTS);
            verify(tableInfoRepository, never()).save(any());
        }

        @Test
        @DisplayName("승인 권한자가 등록하면 승인, 테이블명은 대문자로 저장한다")
        void approver() {
            AccountDto login = SecurityTestSupport.loginAsAdmin(1L);

            service.saveTable(newTable("tb_user"), login);

            ArgumentCaptor<TableInfo> saved = ArgumentCaptor.forClass(TableInfo.class);
            verify(tableInfoRepository).save(saved.capture());
            assertThat(saved.getValue().getTableName()).isEqualTo("TB_USER");
            assertThat(saved.getValue().getProjectId()).isEqualTo(1L);
            assertThat(saved.getValue().getIsApproval()).isTrue();
        }

        @Test
        @DisplayName("일반 사용자가 등록하면 미승인으로 저장한다")
        void user() {
            AccountDto login = SecurityTestSupport.loginAsUser(1L);

            service.saveTable(newTable("TB_USER"), login);

            ArgumentCaptor<TableInfo> saved = ArgumentCaptor.forClass(TableInfo.class);
            verify(tableInfoRepository).save(saved.capture());
            assertThat(saved.getValue().getIsApproval()).isFalse();
        }
    }

    @Test
    @DisplayName("수정 시 변경할 테이블명(대문자)이 이미 있으면 SAVED_DATA_EXISTS")
    void updateDuplicate() {
        AccountDto login = AccountDto.builder().projectId(1L).build();
        TableInfoDto dto = newTable("tb_order");
        dto.setId(1L);
        when(tableInfoRepository.findByIdAndProjectId(1L, 1L)).thenReturn(table(1L, "TB_USER", false));
        when(tableInfoRepository.findAllByTableNameAndProjectId("TB_ORDER", 1L)).thenReturn(List.of(table(2L, "TB_ORDER", true)));

        assertThatThrownBy(() -> service.updateTable(dto, login))
                .extracting("errorCode").isEqualTo(ErrorCode.SAVED_DATA_EXISTS);
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
