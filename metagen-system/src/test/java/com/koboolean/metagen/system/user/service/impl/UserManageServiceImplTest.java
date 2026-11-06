package com.koboolean.metagen.system.user.service.impl;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.security.domain.entity.Role;
import com.koboolean.metagen.core.security.repository.RoleRepository;
import com.koboolean.metagen.core.security.repository.UserRepository;
import com.koboolean.metagen.core.support.CoreFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManageServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserManageServiceImpl service;

    @Test
    @DisplayName("권한 선택 옵션에서 ROLE_ANONYMOUS 는 제외한다")
    void userColumnRoleOptions() {
        when(roleRepository.findAll()).thenReturn(List.of(
                CoreFixtures.role(1L, "ROLE_ADMIN"), CoreFixtures.role(2L, "ROLE_USER"), CoreFixtures.role(3L, "ROLE_ANONYMOUS")));

        ColumnDto roleColumn = service.selectUserColumn().stream()
                .filter(c -> "role".equals(c.getColumn())).findFirst().orElseThrow();

        assertThat(roleColumn.getOptions()).containsExactly("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    @DisplayName("권한이 바뀐 사용자만 새 권한으로 교체한다")
    void saveUserChangesRole() {
        Account account = CoreFixtures.account(5L, "user5", CoreFixtures.role(2L, "ROLE_USER"));
        Role manager = CoreFixtures.role(3L, "ROLE_MANAGER");
        when(userRepository.findById(5L)).thenReturn(Optional.of(account));
        when(roleRepository.findByRoleName("ROLE_MANAGER")).thenReturn(manager);

        service.saveUser(List.of(AccountDto.builder().id("5").role("ROLE_MANAGER").build()));

        assertThat(account.getUserRoles()).containsExactly(manager);
    }

    @Test
    @DisplayName("기본 admin(id=0) 계정은 권한을 바꿀 수 없다")
    void cannotChangeAdminRole() {
        Account admin = CoreFixtures.account(0L, "admin", CoreFixtures.role(1L, "ROLE_ADMIN"));
        when(userRepository.findById(0L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.saveUser(List.of(AccountDto.builder().id("0").role("ROLE_USER").build())))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.MANAGER_NON_DELETED);
    }

    @Test
    @DisplayName("활성 ADMIN 이 1명뿐이면 그 ADMIN 은 삭제할 수 없다")
    void cannotDeleteLastAdmin() {
        Role adminRole = CoreFixtures.role(1L, "ROLE_ADMIN");
        Account admin = CoreFixtures.account(0L, "admin", adminRole);
        when(roleRepository.findByRoleName("ROLE_ADMIN")).thenReturn(adminRole);
        when(userRepository.findById(0L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.deleteUser(List.of(AccountDto.builder().id("0").build())))
                .extracting("errorCode").isEqualTo(ErrorCode.MANAGER_NON_DELETED);
        assertThat(admin.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("사용자 삭제는 비활성화(soft delete)로 처리한다")
    void softDelete() {
        Role adminRole = CoreFixtures.role(1L, "ROLE_ADMIN");
        CoreFixtures.account(0L, "admin", adminRole);
        Account admin2 = CoreFixtures.account(1L, "admin2", adminRole);
        Account user = CoreFixtures.account(5L, "user5", CoreFixtures.role(2L, "ROLE_USER"));
        when(roleRepository.findByRoleName("ROLE_ADMIN")).thenReturn(adminRole);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin2));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        service.deleteUser(List.of(AccountDto.builder().id("1").build(), AccountDto.builder().id("5").build()));

        assertThat(admin2.getIsActive()).isFalse();
        assertThat(user.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("비밀번호 초기화는 아이디로 초기화하고 다음 로그인 시 변경하도록 표시한다")
    void resetPassword() {
        Account account = CoreFixtures.account(5L, "user5");
        account.setPasswdCheck(true);
        when(userRepository.findById(5L)).thenReturn(Optional.of(account));
        when(passwordEncoder.encode("user5")).thenReturn("encoded-user5");

        service.saveUserPassword(AccountDto.builder().id("5").username("user5").build());

        assertThat(account.getPassword()).isEqualTo("encoded-user5");
        assertThat(account.isPasswdCheck()).isFalse();
    }
}
