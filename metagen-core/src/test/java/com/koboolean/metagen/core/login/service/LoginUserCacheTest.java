package com.koboolean.metagen.core.login.service;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginUserCacheTest {

    private final LoginUserCache cache = new LoginUserCache();

    private static AccountDto account(String id, Long projectId) {
        return AccountDto.builder().id(id).username("user" + id).name("이름" + id).projectId(projectId).build();
    }

    @Test
    @DisplayName("로그인 사용자를 id 기준으로 저장하고 프로젝트/이름 정보를 담는다")
    void saveAndGet() {
        cache.save(account("1", 0L));
        cache.save(account("2", 7L));

        Map<String, Map<String, String>> all = cache.getAll();

        assertThat(all).containsOnlyKeys("1", "2");
        assertThat(all.get("2"))
                .containsEntry("username", "user2")
                .containsEntry("name", "이름2")
                .containsEntry("projectId", "7")
                .containsKey("lastActive");
    }

    @Test
    @DisplayName("같은 사용자가 다시 로그인하면 최신 정보로 덮어쓴다")
    void overwrite() {
        cache.save(account("1", 0L));
        cache.save(account("1", 3L));

        assertThat(cache.getAll().get("1")).containsEntry("projectId", "3");
    }

    @Test
    @DisplayName("로그아웃(remove) 하면 목록에서 제거되고, 없는 사용자 extend 는 추가하지 않는다")
    void removeAndExtend() {
        cache.save(account("1", 0L));
        cache.extend("1");
        cache.extend("999");
        assertThat(cache.getAll()).containsOnlyKeys("1");

        cache.remove("1");
        assertThat(cache.getAll()).isEmpty();
    }

    @Test
    @DisplayName("getAll 은 수정할 수 없는 스냅샷을 반환한다")
    void snapshotIsImmutable() {
        cache.save(account("1", 0L));

        assertThatThrownBy(() -> cache.getAll().remove("1")).isInstanceOf(UnsupportedOperationException.class);
    }
}
