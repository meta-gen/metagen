package com.koboolean.metagen.system.project.service.impl;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.project.domain.dto.ProjectDto;
import com.koboolean.metagen.core.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.core.project.domain.entity.ProjectMember;
import com.koboolean.metagen.core.project.domain.event.ProjectDeletedEvent;
import com.koboolean.metagen.core.project.repository.ProjectMemberRepository;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.security.repository.AccountRepository;
import com.koboolean.metagen.core.support.CoreFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectManageServiceImplTest {

    @Mock
    ProjectMemberRepository projectMemberRepository;
    @Mock
    ProjectRepository projectRepository;
    @Mock
    AccountRepository accountRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    ProjectManageServiceImpl service;

    private static AccountDto login(String accountId) {
        return AccountDto.builder().id(accountId).projectId(0L).build();
    }

    @Nested
    @DisplayName("프로젝트 삭제")
    class DeleteProject {

        @Test
        @DisplayName("존재하지 않는 프로젝트는 DATA_CANNOT_BE_DELETED")
        void notFound() {
            when(projectRepository.findById(5L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteProject(5L, login("1")))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        @Test
        @DisplayName("관리자 외 멤버가 있으면 삭제할 수 없다")
        void hasOtherMembers() {
            Project project = CoreFixtures.project(5L, CoreFixtures.account(1L, "manager"));
            CoreFixtures.addMember(project, CoreFixtures.account(2L, "member"), true);
            when(projectRepository.findById(5L)).thenReturn(Optional.of(project));

            assertThatThrownBy(() -> service.deleteProject(5L, login("1")))
                    .hasMessageContaining("프로젝트 관리자 외의 멤버가 존재");
        }

        @Test
        @DisplayName("프로젝트 관리자가 아니면 삭제할 수 없다")
        void notManager() {
            Project project = CoreFixtures.project(5L, CoreFixtures.account(1L, "manager"));
            when(projectRepository.findById(5L)).thenReturn(Optional.of(project));

            assertThatThrownBy(() -> service.deleteProject(5L, login("2")))
                    .hasMessageContaining("프로젝트 관리자 외는 삭제가 불가능");
        }

        @Test
        @DisplayName("기본 프로젝트(id=0)는 삭제할 수 없다")
        void defaultProject() {
            Project project = CoreFixtures.project(0L, CoreFixtures.account(1L, "manager"));
            when(projectRepository.findById(0L)).thenReturn(Optional.of(project));

            assertThatThrownBy(() -> service.deleteProject(0L, login("1")))
                    .hasMessageContaining("기본 프로젝트는 삭제가 불가능");
            verify(projectRepository, never()).delete(any());
        }

        @Test
        @DisplayName("삭제 시 ProjectDeletedEvent 를 먼저 발행한 뒤 멤버와 프로젝트를 삭제한다")
        void success() {
            Project project = CoreFixtures.project(5L, CoreFixtures.account(1L, "manager"));
            List<ProjectMember> members = project.getProjectMember();
            when(projectRepository.findById(5L)).thenReturn(Optional.of(project));

            service.deleteProject(5L, login("1"));

            InOrder order = inOrder(eventPublisher, projectMemberRepository, projectRepository);
            order.verify(eventPublisher).publishEvent(new ProjectDeletedEvent(5L));
            order.verify(projectMemberRepository).deleteAll(members);
            order.verify(projectRepository).delete(project);
        }
    }

    @Test
    @DisplayName("프로젝트 생성 시 생성자를 관리자 겸 활성 멤버로 등록한다")
    void createProject() {
        Account creator = CoreFixtures.account(1L, "creator");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(creator));

        service.createProject(ProjectDto.builder().projectName("신규").isActive(true).isAutoActive(true).isDicAbbrUsed(false).build(), login("1"));

        ArgumentCaptor<Project> project = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(project.capture());
        assertThat(project.getValue().getProjectName()).isEqualTo("신규");
        assertThat(project.getValue().getAccount()).isSameAs(creator);
        assertThat(project.getValue().getProjectMember()).singleElement().satisfies(member -> {
            assertThat(member.getAccount()).isSameAs(creator);
            assertThat(member.getIsActive()).isTrue();
        });
    }

    @Test
    @DisplayName("로그인 프로젝트 관리자 여부")
    void selectLoginProject() {
        Project project = CoreFixtures.project(5L, CoreFixtures.account(1L, "manager"));
        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        when(projectRepository.findById(6L)).thenReturn(Optional.empty());

        assertThat(service.selectLoginProject(5L, "1")).isTrue();
        assertThat(service.selectLoginProject(5L, "2")).isFalse();
        assertThat(service.selectLoginProject(6L, "1")).isFalse();
    }

    @Nested
    @DisplayName("프로젝트 멤버")
    class Members {

        @Test
        @DisplayName("프로젝트 관리자는 멤버에서 삭제할 수 없다 (MANAGER_NON_DELETED)")
        void cannotDeleteManager() {
            Project project = CoreFixtures.project(5L, CoreFixtures.account(1L, "manager"));
            ProjectMember managerMember = project.getProjectMember().get(0);
            when(projectMemberRepository.findById(managerMember.getId())).thenReturn(Optional.of(managerMember));

            ProjectMemberDto dto = ProjectMemberDto.builder().id(managerMember.getId()).accountId(1L).build();

            assertThatThrownBy(() -> service.deleteProjectMember(List.of(dto), login("1")))
                    .extracting("errorCode").isEqualTo(ErrorCode.MANAGER_NON_DELETED);
            verify(projectMemberRepository, never()).delete(any());
        }

        @Test
        @DisplayName("일반 멤버는 삭제된다")
        void deleteMember() {
            Project project = CoreFixtures.project(5L, CoreFixtures.account(1L, "manager"));
            ProjectMember member = CoreFixtures.addMember(project, CoreFixtures.account(2L, "member"), true);
            when(projectMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));

            service.deleteProjectMember(List.of(ProjectMemberDto.builder().id(member.getId()).accountId(2L).build()), login("1"));

            verify(projectMemberRepository).delete(member);
        }

        @Test
        @DisplayName("멤버 승인/미승인 상태를 일괄 변경한다")
        void saveActive() {
            Project project = CoreFixtures.project(5L, CoreFixtures.account(1L, "manager"));
            ProjectMember member = CoreFixtures.addMember(project, CoreFixtures.account(2L, "member"), false);
            when(projectMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));

            service.saveActiveProject(List.of(ProjectMemberDto.builder().id(member.getId()).build()), true);

            assertThat(member.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("멤버 추가 후보는 이미 프로젝트에 속한 계정을 제외한다")
        void selectCandidates() {
            Account manager = CoreFixtures.account(1L, "manager", CoreFixtures.role(1L, "ROLE_USER"));
            Account outsider = CoreFixtures.account(3L, "outsider", CoreFixtures.role(2L, "ROLE_USER"));
            Project project = CoreFixtures.project(5L, manager);
            when(projectMemberRepository.findAllByProject_Id(5L)).thenReturn(project.getProjectMember());
            when(accountRepository.findAll()).thenReturn(List.of(manager, outsider));

            assertThat(service.selectProjectMember(5L, login("1")))
                    .extracting(AccountDto::getUsername).containsExactly("outsider");
        }

        @Test
        @DisplayName("멤버 추가 시 활성 멤버로 저장하고 프로젝트 멤버 목록에도 추가한다")
        void saveMember() {
            Project project = CoreFixtures.project(5L, CoreFixtures.account(1L, "manager"));
            Account newbie = CoreFixtures.account(3L, "newbie");
            when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
            when(accountRepository.findById(3L)).thenReturn(Optional.of(newbie));

            service.saveProjectMember(Map.of("accountId", "3"), 5L);

            ArgumentCaptor<ProjectMember> saved = ArgumentCaptor.forClass(ProjectMember.class);
            verify(projectMemberRepository).save(saved.capture());
            assertThat(saved.getValue().getAccount()).isSameAs(newbie);
            assertThat(saved.getValue().getIsActive()).isTrue();
            assertThat(project.getProjectMember()).contains(saved.getValue());
        }
    }
}
