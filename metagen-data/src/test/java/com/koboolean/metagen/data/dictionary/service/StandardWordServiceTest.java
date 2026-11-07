package com.koboolean.metagen.data.dictionary.service;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTermWordMapping;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardDomainRepository;
import com.koboolean.metagen.data.dictionary.repository.StandardWordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandardWordServiceTest {

    @Mock
    StandardWordRepository standardWordRepository;
    @Mock
    StandardDomainRepository standardDomainRepository;

    @InjectMocks
    StandardWordService service;

    private final AccountDto login = AccountDto.builder().id("1").projectId(1L).build();

    private static StandardWord word(Long id, String name, String domainCategory, boolean approved) {
        return StandardWord.builder()
                .id(id)
                .projectId(1L)
                .commonStandardWordName(name)
                .commonStandardDomainCategory(domainCategory)
                .isApproval(approved)
                .termWordMappings(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("표준단어 삭제")
    class Delete {

        @Test
        @DisplayName("승인된 단어는 삭제할 수 없다")
        void approved() {
            when(standardWordRepository.findByIdAndProjectId(1L, 1L)).thenReturn(word(1L, "사용자", "-", true));

            assertThatThrownBy(() -> service.deleteDataDictionaryStandardWords(List.of(StandardWordDto.builder().id(1L).build()), login))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.APPROVED_DATA_CANNOT_BE_DELETED);
        }

        @Test
        @DisplayName("표준용어에서 사용 중인 단어는 삭제할 수 없다")
        void usedByTerm() {
            StandardWord word = word(1L, "사용자", "-", false);
            word.getTermWordMappings().add(new StandardTermWordMapping());
            when(standardWordRepository.findByIdAndProjectId(1L, 1L)).thenReturn(word);

            assertThatThrownBy(() -> service.deleteDataDictionaryStandardWords(List.of(StandardWordDto.builder().id(1L).build()), login))
                    .extracting("errorCode").isEqualTo(ErrorCode.RELATION_EXISTS);
            verify(standardWordRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("미승인 + 미사용 단어는 삭제한다 (다른 프로젝트 단어는 조회되지 않아 무시)")
        void delete() {
            when(standardWordRepository.findByIdAndProjectId(1L, 1L)).thenReturn(word(1L, "사용자", "-", false));
            when(standardWordRepository.findByIdAndProjectId(2L, 1L)).thenReturn(null);

            service.deleteDataDictionaryStandardWords(List.of(
                    StandardWordDto.builder().id(1L).build(), StandardWordDto.builder().id(2L).build()), login);

            verify(standardWordRepository).deleteById(1L);
            verify(standardWordRepository, never()).deleteById(2L);
        }
    }

    @Nested
    @DisplayName("표준단어 등록")
    class Insert {

        @Test
        @DisplayName("같은 프로젝트에 같은 이름의 단어가 있으면 SAVED_DATA_EXISTS")
        void duplicate() {
            StandardWord word = word(null, "사용자", "-", false);
            when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(1L, "사용자"))
                    .thenReturn(List.of(word(1L, "사용자", "-", true)));

            assertThatThrownBy(() -> service.insertStandardWords(word))
                    .extracting("errorCode").isEqualTo(ErrorCode.SAVED_DATA_EXISTS);
            verify(standardWordRepository, never()).save(any());
        }

        @Test
        @DisplayName("지정한 도메인 분류가 프로젝트에 없으면 DOMAIN_IS_NOT_DEFINED")
        void domainNotDefined() {
            StandardWord word = word(null, "번호", "번호", false);
            when(standardDomainRepository.findAllByProjectIdAndCommonStandardDomainCategory(1L, "번호")).thenReturn(List.of());

            assertThatThrownBy(() -> service.insertStandardWords(word))
                    .extracting("errorCode").isEqualTo(ErrorCode.DOMAIN_IS_NOT_DEFINED);
        }

        @ParameterizedTest(name = "[{index}] 도메인 분류 \"{0}\"")
        @ValueSource(strings = {"", "-"})
        @DisplayName("도메인 분류가 비었거나 '-' 이면 도메인 확인 없이 저장한다")
        void noDomain(String category) {
            StandardWord word = word(null, "사용자", category, false);

            service.insertStandardWords(word);

            verifyNoInteractions(standardDomainRepository);
            verify(standardWordRepository).save(word);
        }

        @Test
        @DisplayName("도메인 분류가 존재하면 저장한다")
        void withDomain() {
            StandardWord word = word(null, "번호", "번호", false);
            when(standardDomainRepository.findAllByProjectIdAndCommonStandardDomainCategory(1L, "번호"))
                    .thenReturn(List.of(new StandardDomain()));

            service.insertStandardWords(word);

            verify(standardWordRepository).save(word);
        }
    }

    @Test
    @DisplayName("승인/미승인 처리는 로그인 프로젝트의 단어에만 적용된다")
    void approval() {
        StandardWord word = word(1L, "사용자", "-", false);
        when(standardWordRepository.findByIdAndProjectId(1L, 1L)).thenReturn(word);

        service.approvalStandardWords(List.of(StandardWordDto.builder().id(1L).build()), login, true);

        assertThat(word.getIsApproval()).isTrue();
    }

    @Test
    @DisplayName("수정 시 금칙어 문자열을 , 로 분리하고 Y/N 값을 boolean 으로 변환한다")
    void update() {
        StandardWord word = word(1L, "사용자", "-", false);
        when(standardWordRepository.findByIdAndProjectId(1L, 1L)).thenReturn(word);

        service.updateStandardWords(StandardWordDto.builder().id(1L).projectId(1L)
                .restrictedWords("유저,회원").useAbbreviation("Y").isFormatWord("N").revisionNumber(2)
                .commonStandardWordDescription("설명").build());

        assertThat(word.getRestrictedWords()).containsExactly("유저", "회원");
        assertThat(word.getUseAbbreviation()).isTrue();
        assertThat(word.getIsFormatWord()).isFalse();
        assertThat(word.getRevisionNumber()).isEqualTo(2);
    }

    @Nested
    @DisplayName("표준단어명/영문약어명에는 '_' 를 쓸 수 없다 (표준용어의 단어 구분자)")
    class Underscore {

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {"사용자_정보|USER", "사용자|USER_ID", "_사용자|USER"})
        @DisplayName("화면 등록 : 단어명 또는 영문약어명에 '_' 가 있으면 WORD_UNDERSCORE_NOT_ALLOWED (저장하지 않음)")
        void insert(String value) {
            String[] parts = value.split("\\|");
            StandardWord word = StandardWord.builder().projectId(1L)
                    .commonStandardWordName(parts[0]).commonStandardWordAbbreviation(parts[1]).commonStandardDomainCategory("-").build();

            assertThatThrownBy(() -> service.insertStandardWords(word))
                    .extracting("errorCode").isEqualTo(ErrorCode.WORD_UNDERSCORE_NOT_ALLOWED);
            verify(standardWordRepository, never()).save(any());
        }

        @Test
        @DisplayName("엑셀 업로드 : '_' 가 들어간 단어가 있으면 업로드 전체를 취소한다")
        void upload() throws java.io.IOException {
            byte[] bytes;
            try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                 java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                workbook.createSheet("용어");
                org.apache.poi.ss.usermodel.Sheet words = workbook.createSheet("단어");
                words.createRow(0).createCell(0).setCellValue("헤더");
                String[][] rows = {
                        {"", "1", "사용자", "USER", "User", "설명", "N", "-", "", ""},
                        {"", "1", "사용자_정보", "USERINFO", "User Info", "설명", "N", "-", "", ""}};
                for (int i = 0; i < rows.length; i++) {
                    org.apache.poi.ss.usermodel.Row row = words.createRow(i + 1);
                    for (int c = 0; c < rows[i].length; c++) row.createCell(c).setCellValue(rows[i][c]);
                }
                workbook.write(out);
                bytes = out.toByteArray();
            }

            assertThatThrownBy(() -> service.setStandardWord(
                    new org.springframework.mock.web.MockMultipartFile("file", "dictionary.xlsx", null, bytes), 1L, true))
                    .extracting("errorCode").isEqualTo(ErrorCode.WORD_UNDERSCORE_NOT_ALLOWED);
            verify(standardWordRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("'_' 가 없으면 통과한다")
        void allowed() {
            org.assertj.core.api.Assertions.assertThatCode(() -> StandardWordService.validateNoUnderscore("사용자", "USER"))
                    .doesNotThrowAnyException();
        }
    }
}
