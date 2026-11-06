package com.koboolean.metagen.design.rule.service.impl;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.support.CoreFixtures;
import com.koboolean.metagen.design.rule.domain.dto.CodeRuleDto;
import com.koboolean.metagen.design.rule.domain.dto.TemplateDto;
import com.koboolean.metagen.design.rule.domain.entity.CodeRule;
import com.koboolean.metagen.design.rule.domain.entity.Template;
import com.koboolean.metagen.design.rule.repository.CodeRuleRepository;
import com.koboolean.metagen.design.rule.repository.TemplateRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeRuleManageServiceImplTest {

    @Mock
    ProjectRepository projectRepository;
    @Mock
    TemplateRepository templateRepository;
    @Mock
    CodeRuleRepository codeRuleRepository;

    @InjectMocks
    CodeRuleManageServiceImpl service;

    private static final Long PROJECT_ID = 1L;
    private final AccountDto login = AccountDto.builder().id("1").projectId(PROJECT_ID).build();

    private static Project project() {
        return CoreFixtures.project(PROJECT_ID, CoreFixtures.account(1L, "admin"));
    }

    private static Template template(Long id) {
        return Template.builder().id(id).projectId(PROJECT_ID).templateName("Controller").codeRules(new ArrayList<>()).build();
    }

    @Nested
    @DisplayName("템플릿")
    class Templates {

        @Test
        @DisplayName("프로젝트가 없으면 템플릿을 등록할 수 없다")
        void noProject() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.saveCodeRuleManageTemplate(PROJECT_ID, TemplateDto.builder().templateName("Controller").build()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        @Test
        @DisplayName("프로젝트 안에서 템플릿명이 중복되면 TEMPLATE_NAME_IN_PROJECT_DUPLICATE")
        void duplicateName() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project()));
            when(templateRepository.findAllByProjectIdAndTemplateName(PROJECT_ID, "Controller")).thenReturn(List.of(template(3L)));

            assertThatThrownBy(() -> service.saveCodeRuleManageTemplate(PROJECT_ID, TemplateDto.builder().templateName("Controller").build()))
                    .extracting("errorCode").isEqualTo(ErrorCode.TEMPLATE_NAME_IN_PROJECT_DUPLICATE);
            verify(templateRepository, never()).save(any());
        }

        @Test
        @DisplayName("템플릿을 프로젝트에 연결해 저장한다")
        void save() {
            Project project = project();
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

            service.saveCodeRuleManageTemplate(PROJECT_ID, TemplateDto.builder().templateName("Service").templateDescription("서비스").build());

            ArgumentCaptor<Template> saved = ArgumentCaptor.forClass(Template.class);
            verify(templateRepository).save(saved.capture());
            assertThat(saved.getValue().getProjectId()).isEqualTo(PROJECT_ID);
            assertThat(saved.getValue().getProject()).isSameAs(project);
            assertThat(saved.getValue().getTemplateName()).isEqualTo("Service");
        }

        @Test
        @DisplayName("없는 템플릿은 삭제할 수 없다")
        void deleteMissing() {
            when(templateRepository.findById(3L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteCodeRuleManageTemplate(3L))
                    .extracting("errorCode").isEqualTo(ErrorCode.DATA_CANNOT_BE_DELETED);
            verify(templateRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("코드규칙")
    class CodeRules {

        private CodeRuleDto request() {
            return CodeRuleDto.builder().templateId(3L).codeRuleName("목록조회").prefix("select").suffix("List").methodForm("${0}").build();
        }

        @Test
        @DisplayName("템플릿 안에서 코드규칙명이 중복되면 TEMPLATE_CODE_RULE_NAME_IN_PROJECT_DUPLICATE")
        void duplicateName() {
            when(templateRepository.findById(3L)).thenReturn(Optional.of(template(3L)));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project()));
            when(codeRuleRepository.findAllByProjectIdAndCodeRuleNameAndTemplate_Id(PROJECT_ID, "목록조회", 3L))
                    .thenReturn(List.of(new CodeRule()));

            assertThatThrownBy(() -> service.saveCodeRuleManage(request(), login))
                    .extracting("errorCode").isEqualTo(ErrorCode.TEMPLATE_CODE_RULE_NAME_IN_PROJECT_DUPLICATE);
            verify(codeRuleRepository, never()).save(any());
        }

        @Test
        @DisplayName("템플릿이 없으면 코드규칙을 등록할 수 없다")
        void noTemplate() {
            when(templateRepository.findById(3L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.saveCodeRuleManage(request(), login))
                    .extracting("errorCode").isEqualTo(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        @Test
        @DisplayName("등록 시 템플릿명을 복사하고 템플릿의 코드규칙 목록에 추가한다")
        void save() {
            Template template = template(3L);
            when(templateRepository.findById(3L)).thenReturn(Optional.of(template));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project()));

            service.saveCodeRuleManage(request(), login);

            ArgumentCaptor<CodeRule> saved = ArgumentCaptor.forClass(CodeRule.class);
            verify(codeRuleRepository).save(saved.capture());
            assertThat(saved.getValue().getProjectId()).isEqualTo(PROJECT_ID);
            assertThat(saved.getValue().getTemplate()).isSameAs(template);
            assertThat(saved.getValue().getTemplateName()).isEqualTo("Controller");
            assertThat(template.getCodeRules()).containsExactly(saved.getValue());
        }

        @Test
        @DisplayName("삭제 시 템플릿의 코드규칙 목록에서도 제거한다")
        void delete() {
            Template template = template(3L);
            CodeRule codeRule = CodeRule.builder().id(5L).template(template).build();
            template.getCodeRules().add(codeRule);
            when(codeRuleRepository.findById(5L)).thenReturn(Optional.of(codeRule));
            when(templateRepository.findById(3L)).thenReturn(Optional.of(template));

            service.deleteCodeRuleManage(PROJECT_ID, List.of(CodeRuleDto.builder().id(5L).build()));

            verify(codeRuleRepository).delete(codeRule);
            assertThat(template.getCodeRules()).isEmpty();
        }

        @Test
        @DisplayName("로그인 프로젝트에 없는 코드규칙 상세 조회는 오류")
        void detailNotFound() {
            when(codeRuleRepository.findByIdAndProjectId(5L, PROJECT_ID)).thenReturn(null);

            assertThatThrownBy(() -> service.selectCodeRuleManageDetail(PROJECT_ID, 5L))
                    .extracting("errorCode").isEqualTo(ErrorCode.DATA_CANNOT_BE_DELETED);
        }
    }
}
