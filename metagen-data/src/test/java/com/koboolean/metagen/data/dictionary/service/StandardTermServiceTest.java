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

    /** 표준용어 '사용자_이름' = 단어(사용자: USER, 이름: NM) + 도메인(명칭V100) */
    private static StandardTermDto termDto() {
        return StandardTermDto.builder()
                .commonStandardTermName("사용자_이름")
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
        @DisplayName("약어에 해당하는 표준단어가 같은 순번의 용어명 단어와 다르면 NOT_FOUND_WORD_DATA")
        void wordMissing() {
            when(standardWordService.findByCommonStandardWordAbbreviation("USER", PROJECT_ID)).thenReturn(List.of(word("사용자", "USER")));
            // NM 약어의 단어는 '번호' 라서 용어명 두 번째 단어 '이름' 과 다르다
            when(standardWordService.findByCommonStandardWordAbbreviation("NM", PROJECT_ID)).thenReturn(List.of(word("번호", "NM")));

            assertThatThrownBy(() -> service.saveStandardTerms(PROJECT_ID, true, termDto(), new String[]{"USER", "NM"}))
                    .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND_WORD_DATA);
            verify(standardTermRepository, never()).save(any());
        }

        @Test
        @DisplayName("용어명과 영문약어명의 '_' 단어 수가 다르면 TERM_WORD_COUNT_MISMATCH (저장/조회하지 않음)")
        void wordCountMismatch() {
            for (String name : List.of("사용자이름", "사용자 이름", "사용자_이름_정보", "사용자__이름", "_사용자_이름")) {
                StandardTermDto dto = termDto();
                dto.setCommonStandardTermName(name);

                assertThatThrownBy(() -> service.saveStandardTerms(PROJECT_ID, true, dto, new String[]{"USER", "NM"}))
                        .as(name)
                        .extracting("errorCode").isEqualTo(ErrorCode.TERM_WORD_COUNT_MISMATCH);
            }
            verify(standardTermRepository, never()).save(any());
        }

        @Test
        @DisplayName("단어 매핑은 같은 순번의 단어명과 정확히 같은 단어를 쓴다 (포함 관계가 아니다)")
        void exactWordMatch() {
            StandardWord user = word("사용자", "USER");
            // '이름' 의 일부 글자를 포함하는 단어가 있어도 정확히 같은 '이름' 을 고른다
            StandardWord name = word("이름", "NM");
            when(standardWordService.findByCommonStandardWordAbbreviation("USER", PROJECT_ID)).thenReturn(List.of(word("사용", "USER"), user));
            when(standardWordService.findByCommonStandardWordAbbreviation("NM", PROJECT_ID)).thenReturn(List.of(name, word("름", "NM")));
            when(standardDomainService.findByCommonStandardDomainNameAndProjectId("명칭V100", PROJECT_ID)).thenReturn(domain());

            service.saveStandardTerms(PROJECT_ID, true, termDto(), new String[]{"USER", "NM"});

            ArgumentCaptor<StandardTerm> saved = ArgumentCaptor.forClass(StandardTerm.class);
            verify(standardTermRepository).save(saved.capture());
            assertThat(saved.getValue().getTermWordMappings()).extracting(StandardTermWordMapping::getStandardWord)
                    .containsExactly(user, name);
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
    @DisplayName("엑셀 업로드 용어명 결정")
    class UploadTermName {

        private final StandardWord user = word("사용자", "USER");
        private final StandardWord name = word("이름", "NM");
        private final java.util.Map<String, List<StandardWord>> words = java.util.Map.of(
                "USER", List.of(user),
                "NM", List.of(word("번호", "NM"), name));

        @Test
        @DisplayName("용어명이 영문약어명과 맞으면 그대로 쓰고 자동 생성 표시를 하지 않는다")
        void keepWhenMatched() {
            StandardTermService.ResolvedTermName resolved = StandardTermService.resolveUploadedTermName("사용자_이름", "USER_NM", words);

            assertThat(resolved.termName()).isEqualTo("사용자_이름");
            assertThat(resolved.words()).containsExactly(user, name);
            assertThat(resolved.generated()).isFalse();
        }

        @Test
        @DisplayName("단어 수가 다르거나 구분자가 없으면 영문약어명의 표준 단어로 용어명을 만든다 (원본에 든 단어 우선)")
        void generateWhenMismatched() {
            for (String original : List.of("사용자이름", "사용자 이름", "사용자_이름_정보", "고객명")) {
                StandardTermService.ResolvedTermName resolved = StandardTermService.resolveUploadedTermName(original, "USER_NM", words);

                assertThat(resolved.generated()).as(original).isTrue();
                assertThat(resolved.words()).as(original).hasSize(2).first().isSameAs(user);
            }
            // 원본에 '이름' 이 있으면 같은 NM 약어의 '번호' 대신 '이름' 을 고른다
            assertThat(StandardTermService.resolveUploadedTermName("사용자이름", "USER_NM", words).termName()).isEqualTo("사용자_이름");
            // 원본에 단서가 없으면 첫 단어
            assertThat(StandardTermService.resolveUploadedTermName("고객명", "USER_NM", words).termName()).isEqualTo("사용자_번호");
        }

        @Test
        @DisplayName("단어 수는 같아도 단어명이 다르면 영문약어명 기준으로 바꾼다")
        void generateWhenWordNameDiffers() {
            StandardTermService.ResolvedTermName resolved = StandardTermService.resolveUploadedTermName("회원_이름", "USER_NM", words);

            assertThat(resolved.termName()).isEqualTo("사용자_이름");
            assertThat(resolved.generated()).isTrue();
        }

        @Test
        @DisplayName("약어에 해당하는 표준 단어가 없으면 NOT_FOUND_WORD_DATA, 영문약어명이 비었으면 TERM_WORD_COUNT_MISMATCH")
        void failWhenNoWord() {
            assertThatThrownBy(() -> StandardTermService.resolveUploadedTermName("사용자_나이", "USER_AGE", words))
                    .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND_WORD_DATA);
            for (String abbr : new String[]{"", "USER__NM", "_NM", null}) {
                assertThatThrownBy(() -> StandardTermService.resolveUploadedTermName("사용자_이름", abbr, words))
                        .as(String.valueOf(abbr))
                        .extracting("errorCode").isEqualTo(ErrorCode.TERM_WORD_COUNT_MISMATCH);
            }
        }

        @Test
        @DisplayName("자동 생성 용어의 설명 끝에 생성 근거를 남기고 1000자를 넘지 않는다")
        void generatedNote() {
            String note = StandardTermService.appendGeneratedNote("사용자의 이름", "USER_NM", "사용자이름");
            assertThat(note).startsWith("사용자의 이름\n" + StandardTermService.GENERATED_NOTE_PREFIX)
                    .contains("USER_NM").contains("원본 표준용어명: 사용자이름");

            assertThat(StandardTermService.appendGeneratedNote(null, "USER_NM", "사용자이름"))
                    .startsWith(StandardTermService.GENERATED_NOTE_PREFIX);

            String longNote = StandardTermService.appendGeneratedNote("가".repeat(1000), "USER_NM", "사용자이름");
            assertThat(longNote).hasSizeLessThanOrEqualTo(1000).contains(StandardTermService.GENERATED_NOTE_PREFIX);
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
            StandardTerm existing = StandardTerm.builder().id(9L).projectId(PROJECT_ID).isApproval(false)
                    .commonStandardTermName("사용자_이름").commonStandardTermAbbreviation("USER_NM").build();
            StandardDomain domain = domain();
            when(standardTermRepository.findByIdAndProjectId(9L, PROJECT_ID)).thenReturn(existing);
            when(standardDomainService.findByCommonStandardDomainNameAndProjectId("명칭V100", PROJECT_ID)).thenReturn(domain);

            service.updateStandardTerms(PROJECT_ID, true, dto, new String[]{});

            assertThat(existing.getCommonStandardTermDescription()).isEqualTo("변경된 설명");
            assertThat(existing.getStandardDomain()).isSameAs(domain);
            assertThat(existing.getIsApproval()).isTrue();
        }

        @Test
        @DisplayName("용어명/약어명을 바꾸지 않으면 이전 형식(공백 구분) 용어도 수정할 수 있다")
        void legacyNameUnchanged() {
            StandardTermDto dto = termDto();
            dto.setId(9L);
            dto.setCommonStandardTermName("사용자 이름");
            StandardTerm existing = StandardTerm.builder().id(9L).projectId(PROJECT_ID).isApproval(false)
                    .commonStandardTermName("사용자 이름").commonStandardTermAbbreviation("USER_NM").build();
            when(standardTermRepository.findByIdAndProjectId(9L, PROJECT_ID)).thenReturn(existing);
            when(standardDomainService.findByCommonStandardDomainNameAndProjectId("명칭V100", PROJECT_ID)).thenReturn(domain());

            service.updateStandardTerms(PROJECT_ID, true, dto, new String[]{});

            assertThat(existing.getIsApproval()).isTrue();
        }

        @Test
        @DisplayName("용어명을 바꿀 때 영문약어명과 단어 수가 다르면 TERM_WORD_COUNT_MISMATCH")
        void changedNameMismatch() {
            StandardTermDto dto = termDto();
            dto.setId(9L);
            dto.setCommonStandardTermName("사용자이름");
            StandardTerm existing = StandardTerm.builder().id(9L).projectId(PROJECT_ID)
                    .commonStandardTermName("사용자_이름").commonStandardTermAbbreviation("USER_NM").build();
            when(standardTermRepository.findByIdAndProjectId(9L, PROJECT_ID)).thenReturn(existing);

            assertThatThrownBy(() -> service.updateStandardTerms(PROJECT_ID, true, dto, new String[]{}))
                    .extracting("errorCode").isEqualTo(ErrorCode.TERM_WORD_COUNT_MISMATCH);
            assertThat(existing.getCommonStandardTermName()).isEqualTo("사용자_이름");
        }
    }
}
