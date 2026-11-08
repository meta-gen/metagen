package com.koboolean.metagen.data.dictionary.service.impl;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.support.SecurityTestSupport;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.service.StandardDomainService;
import com.koboolean.metagen.data.dictionary.service.StandardTermService;
import com.koboolean.metagen.data.dictionary.service.StandardWordService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataDictionaryServiceImplTest {

    @Mock
    StandardWordService standardWordService;
    @Mock
    StandardDomainService standardDomainService;
    @Mock
    StandardTermService standardTermService;

    @InjectMocks
    DataDictionaryServiceImpl service;

    @AfterEach
    void tearDown() {
        SecurityTestSupport.clear();
    }

    private static StandardWordDto newWord() {
        return StandardWordDto.builder()
                .commonStandardWordName("사용자").commonStandardWordAbbreviation("USER").commonStandardWordEnglishName("User")
                .isFormatWord("N").commonStandardDomainCategory("-").synonyms("").restrictedWords("")
                .build();
    }

    @Test
    @DisplayName("승인 권한자가 등록한 표준단어는 승인 + 약어 사용 상태로 저장된다")
    void insertWordAsApprover() {
        AccountDto admin = SecurityTestSupport.loginAsAdmin(7L);

        service.insertStandardWord(admin, newWord());

        ArgumentCaptor<StandardWord> saved = ArgumentCaptor.forClass(StandardWord.class);
        verify(standardWordService).insertStandardWords(saved.capture());
        assertThat(saved.getValue().getProjectId()).isEqualTo(7L);
        assertThat(saved.getValue().getIsApproval()).isTrue();
        assertThat(saved.getValue().getUseAbbreviation()).isTrue();
    }

    @Test
    @DisplayName("일반 사용자가 등록한 표준단어는 미승인 상태로 저장된다")
    void insertWordAsUser() {
        AccountDto user = SecurityTestSupport.loginAsUser(7L);

        service.insertStandardWord(user, newWord());

        ArgumentCaptor<StandardWord> saved = ArgumentCaptor.forClass(StandardWord.class);
        verify(standardWordService).insertStandardWords(saved.capture());
        assertThat(saved.getValue().getIsApproval()).isFalse();
    }

    @Test
    @DisplayName("표준도메인 등록 시 로그인 프로젝트와 승인 여부를 설정한다")
    void insertDomain() {
        AccountDto user = SecurityTestSupport.loginAsUser(7L);

        service.insertStandardDomain(user, StandardDomainDto.builder().commonStandardDomainName("명칭V100").build());

        ArgumentCaptor<StandardDomain> saved = ArgumentCaptor.forClass(StandardDomain.class);
        verify(standardDomainService).insertStandardDomain(saved.capture());
        assertThat(saved.getValue().getProjectId()).isEqualTo(7L);
        assertThat(saved.getValue().getIsApproval()).isFalse();
    }

    @Test
    @DisplayName("표준용어 등록 시 영문약어를 _ 로 분리해 단어 매핑에 사용한다")
    void insertTermSplitsAbbreviation() {
        AccountDto admin = SecurityTestSupport.loginAsAdmin(7L);
        StandardTermDto dto = StandardTermDto.builder().commonStandardTermAbbreviation("USER_NM").build();

        service.insertStandardTerms(admin, dto);

        ArgumentCaptor<String[]> split = ArgumentCaptor.forClass(String[].class);
        verify(standardTermService).saveStandardTerms(eq(7L), eq(true), eq(dto), split.capture());
        assertThat(split.getValue()).containsExactly("USER", "NM");
    }

    @Test
    @DisplayName("용어명 띄어쓰기 단위로 표준단어를 찾고, 마지막 단어의 도메인 분류로 도메인 후보를 조회한다")
    void standardTermList() {
        AccountDto login = AccountDto.builder().projectId(7L).build();
        StandardWord user = StandardWord.builder().id(1L).projectId(7L).commonStandardWordName("사용자")
                .commonStandardDomainCategory("-").isFormatWord(false).isApproval(true).useAbbreviation(false)
                .synonymList(List.of()).restrictedWords(List.of()).build();
        StandardWord name = StandardWord.builder().id(2L).projectId(7L).commonStandardWordName("이름")
                .commonStandardDomainCategory("명칭").isFormatWord(true).isApproval(true).useAbbreviation(false)
                .synonymList(List.of()).restrictedWords(List.of()).build();
        when(standardWordService.commonStandardWordName("사용자", 7L)).thenReturn(user);
        when(standardWordService.commonStandardWordName("이름", 7L)).thenReturn(name);
        when(standardDomainService.getStandardDomains("명칭", 7L)).thenReturn(List.of(
                StandardDomain.builder().id(3L).commonStandardDomainName("명칭V100").isApproval(true).build()));

        Map<String, Object> result = service.getStandardTermList("사용자 이름", login);

        assertThat((List<StandardWordDto>) result.get("list"))
                .extracting(StandardWordDto::getCommonStandardWordName).containsExactly("사용자", "이름");
        assertThat((List<StandardDomainDto>) result.get("standardDomains"))
                .extracting(StandardDomainDto::getCommonStandardDomainName).containsExactly("명칭V100");

        // 표준용어명 형식("_" 구분)으로 조회해도 같은 단어를 찾는다
        assertThat((List<StandardWordDto>) service.getStandardTermList("사용자_이름", login).get("list"))
                .extracting(StandardWordDto::getCommonStandardWordName).containsExactly("사용자", "이름");
    }

    @Test
    @DisplayName("같은 영문약어명 + 도메인의 용어가 있으면 중복으로 알려주고 용어명/승인여부를 함께 준다")
    @SuppressWarnings("unchecked")
    void duplicateTerm() {
        AccountDto login = AccountDto.builder().projectId(1L).build();
        StandardTerm term = StandardTerm.builder().commonStandardTermName("사용자_이름").isApproval(false).build();
        when(standardTermService.findDuplicateTerms("USER_NM", "명칭V100", 1L)).thenReturn(List.of(term));

        Map<String, Object> result = service.checkStandardTermDuplicate("USER_NM", "명칭V100", login);

        assertThat(result.get("duplicated")).isEqualTo(true);
        assertThat((List<Map<String, String>>) result.get("terms")).singleElement().satisfies(t -> {
            assertThat(t.get("commonStandardTermName")).isEqualTo("사용자_이름");
            assertThat(t.get("isApprovalYn")).isEqualTo("N");
        });
    }

    @Test
    @DisplayName("같은 영문약어명 + 도메인의 용어가 없으면 중복 아님")
    void notDuplicateTerm() {
        AccountDto login = AccountDto.builder().projectId(1L).build();
        when(standardTermService.findDuplicateTerms("USER_NM", "명칭V100", 1L)).thenReturn(List.of());

        assertThat(service.checkStandardTermDuplicate("USER_NM", "명칭V100", login).get("duplicated")).isEqualTo(false);
    }
}
