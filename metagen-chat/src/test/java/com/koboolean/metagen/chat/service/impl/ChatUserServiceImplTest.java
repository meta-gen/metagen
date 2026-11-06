package com.koboolean.metagen.chat.service.impl;

import com.koboolean.metagen.chat.service.MessageSaveService;
import com.koboolean.metagen.core.login.service.LoginUserCache;
import com.koboolean.metagen.core.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatUserServiceImplTest {

    @Mock
    UserService userService;
    @Mock
    MessageSaveService messageSaveService;

    private final LoginUserCache loginUserCache = new LoginUserCache();
    private ChatUserServiceImpl service;

    private static AccountDto account(String id, String name) {
        return AccountDto.builder().id(id).username("user" + id).name(name).projectId(7L).build();
    }

    @BeforeEach
    void setUp() {
        service = new ChatUserServiceImpl(userService, loginUserCache, messageSaveService);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("로그인 캐시 기준으로 접속/미접속 사용자를 나누고, 접속자는 프로젝트명과 역할(매니저/사용자)을 표시한다")
    void activeUsers() {
        AccountDto me = account("1", "나");
        AccountDto manager = account("2", "매니저님");
        AccountDto offline = account("3", "오프라인");
        when(userService.getAccountList()).thenReturn(List.of(me, manager, offline));
        loginUserCache.save(me);
        loginUserCache.save(manager);
        when(userService.getProjectName(7L)).thenReturn("프로젝트7");
        when(userService.getProjectRoleName(7L, "user1")).thenReturn(ProjectMemberDto.builder().projectManagerYn("").build());
        when(userService.getProjectRoleName(7L, "user2")).thenReturn(ProjectMemberDto.builder().projectManagerYn("O").build());
        when(messageSaveService.findByBadge("1", "1")).thenReturn("none");
        when(messageSaveService.findByBadge("2", "1")).thenReturn("inline");
        when(messageSaveService.findByBadge("3", "1")).thenReturn("none");

        Map<String, Object> result = service.getActiveUser(me);

        List<Map<String, String>> loggedIn = (List<Map<String, String>>) result.get("loggedIn");
        List<Map<String, String>> loggedOut = (List<Map<String, String>>) result.get("loggedOut");

        assertThat(loggedIn).extracting(u -> u.get("id")).containsExactly("1", "2");
        assertThat(loggedIn.get(0))
                .containsEntry("isMyData", "true")
                .containsEntry("role", "사용자")
                .containsEntry("project", "프로젝트7")
                .containsEntry("status", "active");
        assertThat(loggedIn.get(1))
                .containsEntry("isMyData", "false")
                .containsEntry("role", "매니저")
                .containsEntry("badge", "inline");

        assertThat(loggedOut).singleElement().satisfies(u -> assertThat(u)
                .containsEntry("id", "3")
                .containsEntry("status", "inactive")
                .containsEntry("isMyData", "false"));
    }
}
