package com.koboolean.metagen.data.dictionary.service;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTermWordMapping;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandardTermServiceTest {

    @Mock
    StandardTermRepository standardTermRepository;
    @Mock
    StandardDomainService standardDomainService;
    @Mock
    StandardWordService standardWordService;

    @InjectMocks
    StandardTermService service;

    private static final Long PROJECT_ID = 1L;

    /** 표준용어 '사용자 이름' = 단어(사용자: USER, 이름: NM) + 도메인(명칭V100) */
    private static StandardTermDto termDto() {
        return StandardTermDto.builder()
                .commonStandardTermName("사용자 이름")
                .commonStandardTermAbbreviation("USER_NM")
                .commonStandardDomainName("명칭V100")
                .synonyms("회원명,유저명")
                .build();
    }

    private static StandardWord word(String name, String abbr) {
        return StandardWord.builder().projectId(PROJECT_ID).commonStandardWordName(name).commonStandardWordAbbreviation(abbr).build();
    }

    private static StandardDomain domain() {
        return StandardDomain.builder().id(3L).projectId(PROJECT_ID).commonStandardDomainName("명칭V100").build();
    }

    @Nested
    @DisplayName("표준용어 등록")
    class Save {

        @Test
        @DisplayName("같은 약어 + 도메인의 용어가 있으면 DUPLICATE_TERM_DATA")
        void duplicate() {
            when(standardTermRepository.findByCommonStandardTermAbbreviationAndStandardDomain_CommonStandardDomainNameAndProjectId(
                    "USER_NM", "명칭V100", PROJECT_ID)).thenReturn(List.of(new StandardTerm()));

            assertThatThrownBy(() -> service.saveStandardTerms(PROJECT_ID, true, termDto(), new String[]{"USER", "NM"}))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_TERM_DATA);
        }

        @Test
        @DisplayName("약어에 해당하는 표준단어가 용어명에 없으면 NOT_FOUND_WORD_DATA")
        void wordMissing() {
            when(standardWordService.findByCommonStandardWordAbbreviation("USER", PROJECT_ID)).thenReturn(List.of(word("사용자", "USER")));
            // NM 약어의 단어는 '번호' 라서 용어명 '사용자 이름' 에 포함되지 않는다
            when(standardWordService.findByCommonStandardWordAbbreviation("NM", PROJECT_ID)).thenReturn(List.of(word("번호", "NM")));

            assertThatThrownBy(() -> service.saveStandardTerms(PROJECT_ID, true, termDto(), new String[]{"USER", "NM"}))
                    .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND_WORD_DATA);
            verify(standardTermRepository, never()).save(any());
        }

        @Test
        @DisplayName("도메인이 없으면 NOT_FOUND_DOMAIN_DATA")
        void domainMissing() {
            when(standardWordService.findByCommonStandardWordAbbreviation("USER", PROJECT_ID)).thenReturn(List.of(word("사용자", "USER")));
            when(standardWordService.findByCommonStandardWordAbbreviation("NM", PROJECT_ID)).thenReturn(List.of(word("이름", "NM")));
            when(standardDomainService.findByCommonStandardDomainNameAndProjectId("명칭V100", PROJECT_ID)).thenReturn(null);

            assertThatThrownBy(() -> service.saveStandardTerms(PROJECT_ID, true, termDto(), new String[]{"USER", "NM"}))
                    .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND_DOMAIN_DATA);
        }

        @Test
        @DisplayName("단어 매핑을 약어 순서대로 만들고 용어-도메인 연관과 승인 여부를 설정해 저장한다")
        void success() {
            StandardWord user = word("사용자", "USER");
            StandardWord name = word("이름", "NM");
            StandardDomain domain = domain();
            when(standardWordService.findByCommonStandardWordAbbreviation("USER", PROJECT_ID)).thenReturn(List.of(user));
            when(standardWordService.findByCommonStandardWordAbbreviation("NM", PROJECT_ID)).thenReturn(List.of(word("번호", "NM"), name));
            when(standardDomainService.findByCommonStandardDomainNameAndProjectId("명칭V100", PROJECT_ID)).thenReturn(domain);

            service.saveStandardTerms(PROJECT_ID, false, termDto(), new String[]{"USER", "NM"});

            ArgumentCaptor<StandardTerm> saved = ArgumentCaptor.forClass(StandardTerm.class);
            verify(standardTermRepository).save(saved.capture());
            StandardTerm term = saved.getValue();
            assertThat(term.getProjectId()).isEqualTo(PROJECT_ID);
            assertThat(term.getIsApproval()).isFalse();
            assertThat(term.getStandardDomain()).isSameAs(domain);
            assertThat(term.getSynonymList()).containsExactly("회원명", "유저명");
            assertThat(term.getTermWordMappings())
                    .extracting(StandardTermWordMapping::getStandardWord, StandardTermWordMapping::getOrderIndex)
                    .containsExactly(org.assertj.core.groups.Tuple.tuple(user, 0), org.assertj.core.groups.Tuple.tuple(name, 1));
            assertThat(term.getTermWordMappings()).allSatisfy(m -> assertThat(m.getStandardTerm()).isSameAs(term));
        }
    }

    @Nested
    @DisplayName("표준용어 수정")
    class Update {

        @Test
        @DisplayName("로그인 프로젝트에 없는 용어는 NOT_FOUND_TERM_DATA")
        void notFound() {
            StandardTermDto dto = termDto();
            dto.setId(9L);
            when(standardTermRepository.findByIdAndProjectId(9L, PROJECT_ID)).thenReturn(null);

            assertThatThrownBy(() -> service.updateStandardTerms(PROJECT_ID, true, dto, new String[]{}))
                    .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND_TERM_DATA);
        }

        @Test
        @DisplayName("용어 정보와 도메인 연관을 갱신한다")
        void success() {
            StandardTermDto dto = termDto();
            dto.setId(9L);
            dto.setCommonStandardTermDescription("변경된 설명");
            StandardTerm existing = StandardTerm.builder().id(9L).projectId(PROJECT_ID).isApproval(false).build();
            StandardDomain domain = domain();
            when(standardTermRepository.findByIdAndProjectId(9L, PROJECT_ID)).thenReturn(existing);
            when(standardDomainService.findByCommonStandardDomainNameAndProjectId("명칭V100", PROJECT_ID)).thenReturn(domain);

            service.updateStandardTerms(PROJECT_ID, true, dto, new String[]{});

            assertThat(existing.getCommonStandardTermDescription()).isEqualTo("변경된 설명");
            assertThat(existing.getStandardDomain()).isSameAs(domain);
            assertThat(existing.getIsApproval()).isTrue();
        }
    }
}
