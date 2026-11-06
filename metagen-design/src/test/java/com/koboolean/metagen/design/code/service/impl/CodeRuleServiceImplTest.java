package com.koboolean.metagen.design.code.service.impl;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.support.CoreFixtures;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardWordRepository;
import com.koboolean.metagen.design.code.domain.dto.CodeRuleDetailDto;
import com.koboolean.metagen.design.code.domain.entity.CodeRuleDetail;
import com.koboolean.metagen.design.code.repository.CodeRuleDetailRepository;
import com.koboolean.metagen.design.rule.domain.entity.CodeRule;
import com.koboolean.metagen.design.rule.domain.entity.Template;
import com.koboolean.metagen.design.rule.repository.CodeRuleRepository;
import com.koboolean.metagen.design.rule.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 공통 단어 사전 스텁을 모든 케이스가 쓰지는 않는다
class CodeRuleServiceImplTest {

    @Mock
    CodeRuleDetailRepository codeRuleDetailRepository;
    @Mock
    CodeRuleRepository codeRuleRepository;
    @Mock
    TemplateRepository templateRepository;
    @Mock
    ProjectRepository projectRepository;
    @Mock
    StandardWordRepository standardWordRepository;

    @InjectMocks
    CodeRuleServiceImpl service;

    private static final Long PROJECT_ID = 1L;
    private final AccountDto login = AccountDto.builder().id("1").name("홍길동").projectId(PROJECT_ID).build();
    private Project project;

    private static StandardWord word(String name, String english, String abbr, boolean useAbbreviation) {
        return StandardWord.builder().projectId(PROJECT_ID).commonStandardWordName(name)
                .commonStandardWordEnglishName(english).commonStandardWordAbbreviation(abbr)
                .useAbbreviation(useAbbreviation).build();
    }

    private static CodeRule codeRule(String prefix, String suffix, String description, String methodForm) {
        return CodeRule.builder().id(5L).projectId(PROJECT_ID).codeRuleName("목록조회")
                .prefix(prefix).suffix(suffix).codeRuleDescription(description).methodForm(methodForm)
                .codeRuleDetails(new ArrayList<>()).build();
    }

    private static CodeRuleDetailDto request(String keyword) {
        return CodeRuleDetailDto.builder().codeRuleId(5L).templateId(3L).functionGroup("회원관리")
                .methodKeyword(keyword).useSwagger(false).build();
    }

    @BeforeEach
    void setUp() {
        project = CoreFixtures.project(PROJECT_ID, CoreFixtures.account(1L, "admin"));
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(eq(PROJECT_ID), anyString())).thenReturn(List.of());
        when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(PROJECT_ID, "사용자"))
                .thenReturn(List.of(word("사용자", "User", "USER", false)));
        when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(PROJECT_ID, "이름"))
                .thenReturn(List.of(word("이름", "Name", "NM", false)));
        when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(PROJECT_ID, "접속"))
                .thenReturn(List.of(word("접속", "Internet Protocol", "IP", true)));
        when(standardWordRepository.findAllByProjectIdAndCommonStandardWordName(PROJECT_ID, "주소"))
                .thenReturn(List.of(word("주소", "Home Address", "ADDR", false)));
    }

    private CodeRuleDetailDto generate(CodeRule codeRule, CodeRuleDetailDto request) {
        when(codeRuleRepository.findByIdAndProjectId(5L, PROJECT_ID)).thenReturn(codeRule);
        return service.selectCodeRuleDetail(login, request);
    }

    @Nested
    @DisplayName("메서드명 생성 (접두사 + 키워드 단어 + 접미사 → camelCase)")
    class MethodName {

        @Test
        @DisplayName("약어 미사용 프로젝트는 표준단어 영문명을 사용한다")
        void englishName() {
            CodeRuleDetailDto result = generate(codeRule("select", "List", "${1} 목록을 조회한다", "${0}"), request("사용자 이름"));

            assertThat(result.getMethodName()).isEqualTo("selectUserNameList");
            assertThat(result.getIsDicAbbrUsed()).isFalse();
        }

        @Test
        @DisplayName("약어 사용 프로젝트는 표준단어 영문약어를 사용한다")
        void abbreviation() {
            project.setIsDicAbbrUsed(true);

            CodeRuleDetailDto result = generate(codeRule("select", "List", "${1} 목록을 조회한다", "${0}"), request("사용자 이름"));

            assertThat(result.getMethodName()).isEqualTo("selectUserNmList");
            assertThat(result.getIsDicAbbrUsed()).isTrue();
        }

        @Test
        @DisplayName("단어 자체가 약어 사용으로 지정되어 있으면 프로젝트 설정과 무관하게 약어를 쓰고, 영문명의 공백은 단어 경계가 된다")
        void wordLevelAbbreviationAndSpaces() {
            CodeRuleDetailDto result = generate(codeRule("update", "", "", "${0}"), request("접속 주소"));

            assertThat(result.getMethodName()).isEqualTo("updateIpHomeAddress");
        }

        @Test
        @DisplayName("사전에 없는 키워드는 건너뛰고, 접두사가 비어 있으면 생략한다")
        void unknownWordAndNoPrefix() {
            CodeRuleDetailDto result = generate(codeRule("", "Count", "", "${0}"), request("사용자 미등록단어"));

            assertThat(result.getMethodName()).isEqualTo("userCount");
        }
    }

    @Nested
    @DisplayName("메서드 설명 / 메서드 폼 치환")
    class MethodForm {

        @Test
        @DisplayName("설명에 ${1} 이 있으면 키워드로 치환, 없으면 키워드 뒤에 붙인다")
        void purpose() {
            assertThat(generate(codeRule("select", "List", "${1} 목록을 조회한다", "${0}"), request("사용자"))
                    .getMethodPurpose()).isEqualTo("사용자 목록을 조회한다");
            assertThat(generate(codeRule("select", "List", " 목록 조회", "${0}"), request("사용자"))
                    .getMethodPurpose()).isEqualTo("사용자 목록 조회");
        }

        @Test
        @DisplayName("직접 입력한 목적 설명은 덮어쓰지 않는다")
        void keepPurpose() {
            CodeRuleDetailDto request = request("사용자");
            request.setMethodPurpose("직접 작성한 설명");

            assertThat(generate(codeRule("select", "List", "${1} 조회", "${2}"), request).getMethodForm())
                    .isEqualTo("직접 작성한 설명");
        }

        @Test
        @DisplayName("${0}~${3} 과 ${USER_NAME}/${INPUT}/${OUTPUT}/${EXCEPTION}(대소문자 무시)을 치환한다")
        void placeholders() {
            CodeRuleDetailDto request = request("사용자");
            request.setInput("Long id");
            request.setOutput("UserDto");
            request.setDescription("비고");
            String form = "/** ${2} (${1}) - ${3} @author ${user_name} */ public ${OUTPUT} ${0}(${Input}) throws ${EXCEPTION}";

            CodeRuleDetailDto result = generate(codeRule("select", "", "${1} 조회", form), request);

            assertThat(result.getMethodForm())
                    .isEqualTo("/** 사용자 조회 (사용자) - 비고 @author 홍길동 */ public UserDto selectUser(Long id) throws ");
        }

        @Test
        @DisplayName("스웨거 사용 시 @Operation 어노테이션을 메서드 폼 앞에 붙인다")
        void swagger() {
            CodeRuleDetailDto request = request("사용자");
            request.setUseSwagger(true);

            CodeRuleDetailDto result = generate(codeRule("select", "List", "${1} 목록 조회", "${0}()"), request);

            assertThat(result.getSwaggerData())
                    .isEqualTo("@Operation(summary = \"사용자 목록조회\", description = \"사용자 목록 조회\")\n");
            assertThat(result.getMethodForm()).isEqualTo(result.getSwaggerData() + "selectUserList()");
        }

        @Test
        @DisplayName("코드규칙이 로그인 프로젝트에 없으면 오류")
        void codeRuleNotFound() {
            when(codeRuleRepository.findByIdAndProjectId(5L, PROJECT_ID)).thenReturn(null);

            assertThatThrownBy(() -> service.selectCodeRuleDetail(login, request("사용자")))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DATA_CANNOT_BE_DELETED);
        }
    }

    @Nested
    @DisplayName("코드규칙 상세 등록/수정")
    class Save {

        @Test
        @DisplayName("같은 그룹 + 키워드 + 코드규칙 조합이 있으면 SAVED_DATA_EXISTS")
        void insertDuplicate() {
            when(codeRuleRepository.findByIdAndProjectId(5L, PROJECT_ID)).thenReturn(codeRule("select", "List", "", ""));
            when(templateRepository.findByIdAndProjectId(3L, PROJECT_ID)).thenReturn(new Template());
            when(codeRuleDetailRepository.findByProjectIdAndFunctionGroupAndMethodKeywordAndCodeRule_Id(PROJECT_ID, "회원관리", "사용자", 5L))
                    .thenReturn(new CodeRuleDetail());

            assertThatThrownBy(() -> service.insertCodeRule(login, request("사용자")))
                    .extracting("errorCode").isEqualTo(ErrorCode.SAVED_DATA_EXISTS);
            verify(codeRuleDetailRepository, never()).save(any());
        }

        @Test
        @DisplayName("템플릿이 없으면 등록할 수 없다")
        void insertWithoutTemplate() {
            when(codeRuleRepository.findByIdAndProjectId(5L, PROJECT_ID)).thenReturn(codeRule("select", "List", "", ""));
            when(templateRepository.findByIdAndProjectId(3L, PROJECT_ID)).thenReturn(null);

            assertThatThrownBy(() -> service.insertCodeRule(login, request("사용자")))
                    .extracting("errorCode").isEqualTo(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        @Test
        @DisplayName("등록 시 로그인 프로젝트로 저장하고 코드규칙의 상세 목록에 추가한다")
        void insert() {
            CodeRule codeRule = codeRule("select", "List", "", "");
            when(codeRuleRepository.findByIdAndProjectId(5L, PROJECT_ID)).thenReturn(codeRule);
            when(templateRepository.findByIdAndProjectId(3L, PROJECT_ID)).thenReturn(new Template());

            CodeRuleDetailDto request = request("사용자");
            request.setMethodName("selectUserList");
            service.insertCodeRule(login, request);

            ArgumentCaptor<CodeRuleDetail> saved = ArgumentCaptor.forClass(CodeRuleDetail.class);
            verify(codeRuleDetailRepository).save(saved.capture());
            assertThat(saved.getValue().getProjectId()).isEqualTo(PROJECT_ID);
            assertThat(saved.getValue().getMethodName()).isEqualTo("selectUserList");
            assertThat(saved.getValue().getCodeRule()).isSameAs(codeRule);
            assertThat(codeRule.getCodeRuleDetails()).containsExactly(saved.getValue());
        }

        @Test
        @DisplayName("수정 시 다른 상세와 그룹 + 키워드가 겹치면 SAVED_DATA_EXISTS (자기 자신은 허용)")
        void updateDuplicate() {
            CodeRuleDetailDto request = request("사용자");
            request.setId(10L);
            when(codeRuleDetailRepository.findByProjectIdAndFunctionGroupAndMethodKeywordAndCodeRule_Id(PROJECT_ID, "회원관리", "사용자", 5L))
                    .thenReturn(CodeRuleDetail.builder().id(11L).build());

            assertThatThrownBy(() -> service.updateCodeRule(login, request))
                    .extracting("errorCode").isEqualTo(ErrorCode.SAVED_DATA_EXISTS);
        }

        @Test
        @DisplayName("수정 시 코드규칙이 바뀌면 이전 규칙의 상세 목록에서 빼고 새 규칙에 추가한다")
        void updateMovesRule() {
            CodeRule oldRule = codeRule("select", "List", "", "");
            CodeRule newRule = codeRule("update", "", "", "");
            newRule.setId(6L);
            CodeRuleDetail detail = CodeRuleDetail.builder().id(10L).projectId(PROJECT_ID).codeRule(oldRule).build();
            oldRule.getCodeRuleDetails().add(detail);
            CodeRuleDetailDto request = request("사용자");
            request.setId(10L);
            request.setCodeRuleId(6L);
            request.setMethodName("updateUser");
            when(codeRuleDetailRepository.findByProjectIdAndFunctionGroupAndMethodKeywordAndCodeRule_Id(PROJECT_ID, "회원관리", "사용자", 6L))
                    .thenReturn(detail);
            when(codeRuleDetailRepository.findByIdAndProjectId(10L, PROJECT_ID)).thenReturn(detail);
            when(codeRuleRepository.findById(6L)).thenReturn(Optional.of(newRule));
            when(codeRuleRepository.findByIdAndProjectId(5L, PROJECT_ID)).thenReturn(oldRule);

            service.updateCodeRule(login, request);

            assertThat(detail.getMethodName()).isEqualTo("updateUser");
            assertThat(detail.getCodeRule()).isSameAs(newRule);
            assertThat(oldRule.getCodeRuleDetails()).isEmpty();
            assertThat(newRule.getCodeRuleDetails()).containsExactly(detail);
        }
    }
}
