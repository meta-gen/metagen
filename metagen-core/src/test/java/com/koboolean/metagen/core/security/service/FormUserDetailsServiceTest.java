package com.koboolean.metagen.core.security.service;

import com.koboolean.metagen.core.security.domain.dto.AccountContext;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.security.repository.UserRepository;
import com.koboolean.metagen.core.support.CoreFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormUserDetailsServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    FormUserDetailsService service;

    @Test
    @DisplayName("활성 계정이 없으면 UsernameNotFoundException")
    void notFound() {
        when(userRepository.findByUsernameAndIsActive("ghost", true)).thenReturn(null);

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    @DisplayName("계정의 권한(Role)을 GrantedAuthority 로 변환해 AccountContext 를 만든다")
    void load() {
        Account account = CoreFixtures.account(1L, "admin", CoreFixtures.role(1L, "ROLE_ADMIN"));
        when(userRepository.findByUsernameAndIsActive("admin", true)).thenReturn(account);

        UserDetails details = service.loadUserByUsername("admin");

        assertThat(details).isInstanceOf(AccountContext.class);
        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMIN");
        assertThat(((AccountContext) details).getAccountDto().getUsername()).isEqualTo("admin");
    }
}
