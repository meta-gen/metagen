package com.koboolean.metagen.system.systemLog.service.impl;

import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.core.logs.repository.LogsRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemLogServiceImplTest {

    @Mock
    LogsRepository logsRepository;

    @InjectMocks
    SystemLogServiceImpl service;

    private final Pageable pageable = PageRequest.of(0, 10);
    private final AccountDto login = AccountDto.builder().id("1").projectId(3L).build();

    @Test
    @DisplayName("projectId 가 없으면 조회하지 않는다")
    void requireProject() {
        assertThatThrownBy(() -> service.getSystemLogData(pageable, AccountDto.builder().build(), null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "  ")
    @DisplayName("검색어가 없으면 로그인 프로젝트의 전체 로그를 조회한다")
    void noSearch(String query) {
        when(logsRepository.findAllByProjectId(3L, pageable)).thenReturn(Page.empty());

        service.getSystemLogData(pageable, login, query, "ip");

        verify(logsRepository).findAllByProjectId(3L, pageable);
        verifyNoMoreInteractions(logsRepository);
    }

    @Test
    @DisplayName("검색 컬럼에 맞는 프로젝트 범위 검색 쿼리를 사용한다")
    void searchByColumn() {
        when(logsRepository.findByProjectIdAndIpContaining(3L, "127.0", pageable)).thenReturn(Page.empty());
        when(logsRepository.findByProjectIdAndErrorMessageContaining(3L, "NPE", pageable)).thenReturn(Page.empty());

        service.getSystemLogData(pageable, login, "127.0", "ip");
        service.getSystemLogData(pageable, login, "NPE", "errorMessage");

        verify(logsRepository).findByProjectIdAndIpContaining(3L, "127.0", pageable);
        verify(logsRepository).findByProjectIdAndErrorMessageContaining(3L, "NPE", pageable);
    }

    @Test
    @DisplayName("알 수 없는 검색 컬럼은 전체 조회로 대체한다")
    void unknownColumnFallback() {
        when(logsRepository.findAllByProjectId(3L, pageable)).thenReturn(Page.empty());

        service.getSystemLogData(pageable, login, "x", "userAgent");

        verify(logsRepository).findAllByProjectId(3L, pageable);
    }

    @Test
    @DisplayName("요청/응답 본문 등 상세 컬럼은 그리드에서 숨긴다")
    void hiddenColumns() {
        assertThat(service.getSystemLogColumn())
                .filteredOn(column -> Boolean.FALSE.equals(column.getIsVisible()))
                .extracting(ColumnDto::getColumn)
                .containsExactlyInAnyOrder("requestBody", "responseBody", "errorMessage", "userAgent");
    }
}
