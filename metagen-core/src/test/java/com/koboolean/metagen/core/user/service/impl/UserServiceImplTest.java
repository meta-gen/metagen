package com.koboolean.metagen.core.user.service.impl;

import com.koboolean.metagen.common.exception.CustomFormException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.core.project.domain.entity.ProjectMember;
import com.koboolean.metagen.core.project.repository.ProjectMemberRepository;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.security.domain.entity.Role;
import com.koboolean.metagen.core.security.repository.RoleRepository;
import com.koboolean.metagen.core.security.repository.UserRepository;
import com.koboolean.metagen.core.support.CoreFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ProjectMemberRepository projectMemberRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    ProjectRepository projectRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserServiceImpl userService;

    private static AccountDto signUp(String username, String password) {
        return AccountDto.builder().username(username).name("신규").password(password).projectId(1L).build();
    }

    @Nested
    @DisplayName("회원가입")
    class CreateUser {

        @Test
        @DisplayName("이미 등록된 아이디면 USERNAME_IS_DUPLICATION")
        void duplicateUsername() {
            when(userRepository.findByUsername("new_user1")).thenReturn(CoreFixtures.account(5L, "new_user1"));

            assertThatThrownBy(() -> userService.createUser(signUp("new_user1", "Passw0rd!")))
                    .isInstanceOf(CustomFormException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.USERNAME_IS_DUPLICATION);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("비밀번호 규칙 위반이면 PASSWORD_IS_NON_VALIDATOR")
        void invalidPassword() {
            assertThatThrownBy(() -> userService.createUser(signUp("new_user1", "weakpass")))
                    .extracting("errorCode").isEqualTo(ErrorCode.PASSWORD_IS_NON_VALIDATOR);
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @ValueSource(strings = {"abc", "_user1", "user1_", "us__er", "User1", "a_very_long_username_over20"})
        @DisplayName("아이디 규칙(5~20자 소문자/숫자/_, 처음·끝·연속 _ 금지) 위반이면 USERNAME_IS_NON_VALIDATOR")
        void invalidUsername(String username) {
            assertThatThrownBy(() -> userService.createUser(signUp(username, "Passw0rd!")))
                    .extracting("errorCode").isEqualTo(ErrorCode.USERNAME_IS_NON_VALIDATOR);
        }

        @Test
        @DisplayName("가입 시 미승인(ROLE_NOT_APPROVE) 권한으로 저장하고, 프로젝트 자동승인 설정을 멤버 활성화 여부로 사용한다")
        void success() {
            Role notApprove = CoreFixtures.role(9L, "ROLE_NOT_APPROVE");
            Project project = CoreFixtures.project(1L, CoreFixtures.account(1L, "admin"));
            project.setIsAutoActive(true);
            when(passwordEncoder.encode("Passw0rd!")).thenReturn("encoded!");
            when(roleRepository.findByRoleName("ROLE_NOT_APPROVE")).thenReturn(notApprove);
            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

            userService.createUser(signUp("new_user1", "Passw0rd!"));

            ArgumentCaptor<Account> account = ArgumentCaptor.forClass(Account.class);
            verify(userRepository).save(account.capture());
            assertThat(account.getValue().getUsername()).isEqualTo("new_user1");
            assertThat(account.getValue().getPassword()).isEqualTo("encoded!");
            assertThat(account.getValue().getIsActive()).isTrue();
            assertThat(account.getValue().getUserRoles()).containsExactly(notApprove);

            ArgumentCaptor<ProjectMember> member = ArgumentCaptor.forClass(ProjectMember.class);
            verify(projectMemberRepository).save(member.capture());
            assertThat(member.getValue().getProject()).isSameAs(project);
            assertThat(member.getValue().getAccount()).isSameAs(account.getValue());
            assertThat(member.getValue().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트로 가입하면 IllegalArgumentException")
        void projectNotFound() {
            when(roleRepository.findByRoleName("ROLE_NOT_APPROVE")).thenReturn(CoreFixtures.role(9L, "ROLE_NOT_APPROVE"));
            when(projectRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser(signUp("new_user1", "Passw0rd!")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("프로젝트명 조회 : 없는 프로젝트는 NAN")
    void getProjectName() {
        Project project = CoreFixtures.project(3L, CoreFixtures.account(1L, "admin"));
        when(projectRepository.findById(3L)).thenReturn(Optional.of(project));
        when(projectRepository.findById(4L)).thenReturn(Optional.empty());

        assertThat(userService.getProjectName(3L)).isEqualTo("프로젝트3");
        assertThat(userService.getProjectName(4L)).isEqualTo("NAN");
    }

    @Test
    @DisplayName("권한 설명 조회")
    void getRoleName() {
        when(roleRepository.findByRoleName("ROLE_ADMIN")).thenReturn(CoreFixtures.role(1L, "ROLE_ADMIN"));

        assertThat(userService.getRoleName("ROLE_ADMIN")).isEqualTo("ROLE_ADMIN_DESC");
    }

    @Test
    @DisplayName("관리 중인 프로젝트 조회는 관리자 = 멤버 = 로그인 사용자 조건으로 조회한다")
    void selectAllProjectsByUsernameProjectManager() {
        Account me = CoreFixtures.account(5L, "me");
        when(projectRepository.findAllByAccount_IdAndProjectMember_Account_Id(5L, 5L))
                .thenReturn(List.of(CoreFixtures.project(2L, me)));

        assertThat(userService.selectAllProjectsByUsernameProjectManager(AccountDto.builder().id("5").build()))
                .singleElement()
                .satisfies(p -> assertThat(p.getId()).isEqualTo(2L));
    }
}
