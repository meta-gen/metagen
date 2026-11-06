package com.koboolean.metagen.core.login.service.impl;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.project.domain.dto.ProjectDto;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.security.repository.UserRepository;
import com.koboolean.metagen.core.support.CoreFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserRepository userRepository;
    @Mock
    ProjectRepository projectRepository;

    @InjectMocks
    LoginServiceImpl loginService;

    @Nested
    @DisplayName("로그인 1단계 : 접근 가능한 프로젝트 조회")
    class SelectLoginProjects {

        @Test
        @DisplayName("계정이 없거나 비활성이면 LOGIN_FAILED")
        void accountNotFound() {
            when(userRepository.findByUsernameAndIsActive("ghost", true)).thenReturn(null);

            assertThatThrownBy(() -> loginService.selectLoginProjects("ghost", "pw"))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.LOGIN_FAILED);
        }

        @Test
        @DisplayName("비밀번호가 null 이거나 틀리면 LOGIN_FAILED")
        void wrongPassword() {
            Account account = CoreFixtures.account(1L, "admin");
            when(userRepository.findByUsernameAndIsActive("admin", true)).thenReturn(account);
            when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

            assertThatThrownBy(() -> loginService.selectLoginProjects("admin", "wrong"))
                    .extracting("errorCode").isEqualTo(ErrorCode.LOGIN_FAILED);
            assertThatThrownBy(() -> loginService.selectLoginProjects("admin", null))
                    .extracting("errorCode").isEqualTo(ErrorCode.LOGIN_FAILED);
        }

        @Test
        @DisplayName("접근 가능한 프로젝트가 없으면 NO_ACCESSIBLE_PROJECT")
        void noProject() {
            Account account = CoreFixtures.account(1L, "admin");
            when(userRepository.findByUsernameAndIsActive("admin", true)).thenReturn(account);
            when(passwordEncoder.matches("pw", "encoded")).thenReturn(true);
            when(projectRepository.findProjectsByUsername("admin")).thenReturn(List.of());

            assertThatThrownBy(() -> loginService.selectLoginProjects("admin", "pw"))
                    .extracting("errorCode").isEqualTo(ErrorCode.NO_ACCESSIBLE_PROJECT);
        }

        @Test
        @DisplayName("인증에 성공하면 사용자의 프로젝트 목록을 반환한다")
        void success() {
            Account account = CoreFixtures.account(1L, "admin");
            when(userRepository.findByUsernameAndIsActive("admin", true)).thenReturn(account);
            when(passwordEncoder.matches("pw", "encoded")).thenReturn(true);
            when(projectRepository.findProjectsByUsername("admin"))
                    .thenReturn(List.of(CoreFixtures.project(0L, account), CoreFixtures.project(7L, account)));

            List<ProjectDto> projects = loginService.selectLoginProjects("admin", "pw");

            assertThat(projects).extracting(ProjectDto::getId).containsExactly(0L, 7L);
            assertThat(projects).extracting(ProjectDto::getProjectManagerName).containsOnly("admin_name");
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    class UpdatePwd {

        private final AccountDto login = AccountDto.builder().username("admin").build();

        @Test
        @DisplayName("현재 비밀번호가 틀리면 PASSWORD_NOT_MATCHED")
        void currentPasswordMismatch() {
            Account account = CoreFixtures.account(1L, "admin");
            when(userRepository.findByUsername("admin")).thenReturn(account);
            when(passwordEncoder.matches("old", "encoded")).thenReturn(false);

            AccountDto request = AccountDto.builder().password("old").currentPassword("NewPassw0rd!").build();

            assertThatThrownBy(() -> loginService.updatePwd(login, request))
                    .extracting("errorCode").isEqualTo(ErrorCode.PASSWORD_NOT_MATCHED);
        }

        @Test
        @DisplayName("새 비밀번호가 규칙에 맞지 않으면 PASSWORD_IS_NON_VALIDATOR")
        void invalidNewPassword() {
            Account account = CoreFixtures.account(1L, "admin");
            when(userRepository.findByUsername("admin")).thenReturn(account);
            when(passwordEncoder.matches("old", "encoded")).thenReturn(true);

            AccountDto request = AccountDto.builder().password("old").currentPassword("weak").build();

            assertThatThrownBy(() -> loginService.updatePwd(login, request))
                    .extracting("errorCode").isEqualTo(ErrorCode.PASSWORD_IS_NON_VALIDATOR);
        }

        @Test
        @DisplayName("성공 시 새 비밀번호를 암호화해 저장하고 비밀번호 변경 완료로 표시한다")
        void success() {
            Account account = CoreFixtures.account(1L, "admin");
            account.setPasswdCheck(false);
            when(userRepository.findByUsername("admin")).thenReturn(account);
            when(passwordEncoder.matches("old", "encoded")).thenReturn(true);
            when(passwordEncoder.encode("NewPassw0rd!")).thenReturn("new-encoded");

            loginService.updatePwd(login, AccountDto.builder().password("old").currentPassword("NewPassw0rd!").build());

            assertThat(account.getPassword()).isEqualTo("new-encoded");
            assertThat(account.isPasswdCheck()).isTrue();
        }
    }

    @Test
    @DisplayName("이름 변경은 로그인 사용자의 계정 이름을 바꾼다")
    void updateName() {
        Account account = CoreFixtures.account(1L, "admin");
        when(userRepository.findByUsername("admin")).thenReturn(account);

        loginService.updateName(AccountDto.builder().username("admin").build(), "새이름");

        assertThat(account.getName()).isEqualTo("새이름");
    }
}
