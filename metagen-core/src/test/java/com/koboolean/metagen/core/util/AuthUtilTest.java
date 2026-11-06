package com.koboolean.metagen.core.util;

import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.core.project.repository.ProjectRepository;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.support.CoreFixtures;
import com.koboolean.metagen.core.support.SecurityTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthUtilTest {

    @AfterEach
    void tearDown() {
        SecurityTestSupport.clear();
    }

    @ParameterizedTest(name = "[{index}] {0} → {1}")
    @CsvSource({
            "Passw0rd!, true",
            "ABCDEFG!, true",
            "password!, false",   // 대문자 없음
            "Password1, false",   // 특수문자 없음
            "Pa!1, false",        // 8자 미만
            "Pass word!, false"   // 허용되지 않는 문자(공백)
    })
    @DisplayName("비밀번호 규칙 : 8자 이상, 대문자/특수문자 각 1개 이상")
    void isValidPassword(String password, boolean expected) {
        assertThat(AuthUtil.isValidPassword(password)).isEqualTo(expected);
    }

    @Test
    @DisplayName("ADMIN / MANAGER 권한은 프로젝트 관리자가 아니어도 승인 가능")
    void approvalByRole() {
        SecurityTestSupport.loginAs(1L, "9", "ROLE_ADMIN");
        assertThat(AuthUtil.isApprovalAvailable()).isTrue();

        SecurityTestSupport.loginAs(1L, "9", "ROLE_MANAGER");
        assertThat(AuthUtil.isApprovalAvailable()).isTrue();
    }

    @Test
    @DisplayName("일반 사용자라도 로그인한 프로젝트의 관리자면 승인 가능")
    void approvalByProjectManager() {
        Account manager = CoreFixtures.account(2L, "manager");
        Project project = CoreFixtures.project(1L, manager);
        ProjectRepository projectRepository = mock(ProjectRepository.class);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        SecurityTestSupport.loginAs(projectRepository, 1L, "2", "ROLE_USER");
        assertThat(AuthUtil.isApprovalAvailable()).isTrue();

        SecurityTestSupport.loginAs(projectRepository, 1L, "3", "ROLE_USER");
        assertThat(AuthUtil.isApprovalAvailable()).isFalse();
    }

    @Test
    @DisplayName("프로젝트가 없고 권한도 USER 이면 승인 불가")
    void noApproval() {
        SecurityTestSupport.loginAsUser(1L);

        assertThat(AuthUtil.isApprovalAvailable()).isFalse();
    }

    @Test
    @DisplayName("isApprovalAdmin 은 ADMIN 권한만 true")
    void approvalAdmin() {
        SecurityTestSupport.loginAs(1L, "1", "ROLE_ADMIN");
        assertThat(AuthUtil.isApprovalAdmin()).isTrue();

        SecurityTestSupport.loginAs(1L, "1", "ROLE_MANAGER");
        assertThat(AuthUtil.isApprovalAdmin()).isFalse();
    }
}
