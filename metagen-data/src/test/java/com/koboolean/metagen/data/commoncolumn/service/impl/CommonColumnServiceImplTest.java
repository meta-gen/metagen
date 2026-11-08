package com.koboolean.metagen.data.commoncolumn.service.impl;

import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.support.SecurityTestSupport;
import com.koboolean.metagen.data.commoncolumn.domain.dto.CommonColumnDto;
import com.koboolean.metagen.data.commoncolumn.domain.entity.CommonColumn;
import com.koboolean.metagen.data.commoncolumn.repository.CommonColumnRepository;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommonColumnServiceImplTest {

    @Mock
    CommonColumnRepository commonColumnRepository;
    @Mock
    StandardTermRepository standardTermRepository;

    @InjectMocks
    CommonColumnServiceImpl service;

    @AfterEach
    void tearDown() {
        SecurityTestSupport.clear();
    }

    private static StandardTerm term(long id, long projectId, String abbreviation) {
        return StandardTerm.builder().id(id).projectId(projectId).commonStandardTermName(abbreviation).commonStandardTermAbbreviation(abbreviation).isApproval(true)
                .termWordMappings(new ArrayList<>()).synonymList(new ArrayList<>()).columnInfos(new ArrayList<>()).build();
    }

    private static CommonColumnDto item(Long termId, String nullable, String defaultValue) {
        return CommonColumnDto.builder().termId(termId).isNullable(nullable).isIndex("N").defaultValue(defaultValue).build();
    }

    @Test
    @DisplayName("조회 : 순서대로 용어 정보를 붙이고, 지워졌거나 다른 프로젝트 용어는 term=null")
    void select() {
        AccountDto login = SecurityTestSupport.loginAsUser(1L);
        when(commonColumnRepository.findAllByProjectIdOrderBySortOrderAscIdAsc(1L)).thenReturn(List.of(
                CommonColumn.builder().id(1L).projectId(1L).termId(10L).sortOrder(1).isNullable(false).defaultValue("CURRENT_TIMESTAMP").build(),
                CommonColumn.builder().id(2L).projectId(1L).termId(11L).sortOrder(2).isNullable(true).build(),
                CommonColumn.builder().id(3L).projectId(1L).termId(12L).sortOrder(3).build()));
        when(standardTermRepository.findAllById(anyIterable())).thenReturn(List.of(term(10L, 1L, "REG_DT"), term(12L, 2L, "OTHER")));

        List<CommonColumnDto> result = service.selectCommonColumns(login);

        assertThat(result).extracting(CommonColumnDto::getTermId, CommonColumnDto::getIsNullable, CommonColumnDto::getDefaultValue)
                .containsExactly(tuple(10L, "N", "CURRENT_TIMESTAMP"), tuple(11L, "Y", null), tuple(12L, "N", null));
        assertThat(result.get(0).getTerm()).isNotNull();
        assertThat(result.get(1).getTerm()).as("지워진 용어").isNull();
        assertThat(result.get(2).getTerm()).as("다른 프로젝트 용어").isNull();
    }

    @Test
    @DisplayName("저장 : 프로젝트 관리자는 세트를 목록 순서대로 통째로 바꾼다")
    void replace() {
        AccountDto login = SecurityTestSupport.loginAsAdmin(1L);
        List<CommonColumn> old = List.of(CommonColumn.builder().id(9L).projectId(1L).termId(99L).build());
        when(commonColumnRepository.findAllByProjectIdOrderBySortOrderAscIdAsc(1L)).thenReturn(old);
        when(standardTermRepository.findAllById(anyIterable())).thenReturn(List.of(term(10L, 1L, "REG_DT"), term(11L, 1L, "USE_YN")));

        service.replaceCommonColumns(login, List.of(item(11L, "N", " N "), item(10L, "N", "CURRENT_TIMESTAMP")));

        verify(commonColumnRepository).deleteAll(old);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CommonColumn>> saved = ArgumentCaptor.forClass(List.class);
        verify(commonColumnRepository).saveAll(saved.capture());
        assertThat(saved.getValue()).extracting(CommonColumn::getTermId, CommonColumn::getSortOrder, CommonColumn::getDefaultValue, CommonColumn::getProjectId)
                .containsExactly(tuple(11L, 1, "N", 1L), tuple(10L, 2, "CURRENT_TIMESTAMP", 1L));
    }

    @Test
    @DisplayName("저장 : 일반 사용자는 바꿀 수 없다 (COMMON_COLUMN_PERMISSION_DENIED)")
    void replaceDenied() {
        AccountDto login = SecurityTestSupport.loginAsUser(1L);

        assertThatThrownBy(() -> service.replaceCommonColumns(login, List.of(item(10L, "N", null))))
                .extracting("errorCode").isEqualTo(ErrorCode.COMMON_COLUMN_PERMISSION_DENIED);
        verify(commonColumnRepository, never()).saveAll(anyCollection());
    }

    @Test
    @DisplayName("저장 : 없는/다른 프로젝트 용어는 NOT_FOUND_TERM_DATA, 같은 컬럼명 두 번은 COMMON_COLUMN_DUPLICATED (아무것도 지우지 않음)")
    void replaceInvalid() {
        AccountDto login = SecurityTestSupport.loginAsAdmin(1L);
        when(standardTermRepository.findAllById(anyIterable())).thenReturn(List.of(term(10L, 1L, "REG_DT"), term(13L, 2L, "OTHER")));

        assertThatThrownBy(() -> service.replaceCommonColumns(login, List.of(item(10L, "N", null), item(13L, "N", null))))
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND_TERM_DATA);

        when(standardTermRepository.findAllById(anyIterable())).thenReturn(List.of(term(10L, 1L, "REG_DT"), term(14L, 1L, "REG_DT")));
        assertThatThrownBy(() -> service.replaceCommonColumns(login, List.of(item(10L, "N", null), item(14L, "N", null))))
                .extracting("errorCode").isEqualTo(ErrorCode.COMMON_COLUMN_DUPLICATED);

        verify(commonColumnRepository, never()).deleteAll(any());
    }
}
