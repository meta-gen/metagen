package com.koboolean.metagen.user.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 현재 로그인된 사용자 정보 (단일 서버 기준 In-Memory 캐시)
 * 마지막 요청 이후 30분이 지나면 자동 만료된다.
 */
@Component
public class LoginUserCache {

    private final Cache<String, Map<String, String>> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();

    /**
     * 로그인 사용자 저장
     */
    public void save(AccountDto accountDto) {
        Map<String, String> userData = new HashMap<>();
        userData.put("username", accountDto.getUsername());
        userData.put("name", accountDto.getName());
        userData.put("projectId", accountDto.getProjectId().toString());
        userData.put("lastActive", String.valueOf(System.currentTimeMillis()));

        cache.put(accountDto.getId(), userData);
    }

    /**
     * 로그인 상태인 사용자의 만료시간 연장
     */
    public void extend(String id) {
        cache.asMap().computeIfPresent(id, (key, value) -> value);
    }

    /**
     * 로그아웃 시 사용자 제거
     */
    public void remove(String id) {
        cache.invalidate(id);
    }

    /**
     * 로그인된 전체 사용자 (key : 사용자 id)
     */
    public Map<String, Map<String, String>> getAll() {
        return Map.copyOf(cache.asMap());
    }
}
