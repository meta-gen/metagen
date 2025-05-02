package com.koboolean.metagen.home.conf;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class JpaConfig {

    private static final Logger logger = LoggerFactory.getLogger(JpaConfig.class);

    // createBy, updateBy에 session에 존재하는 name으로 사용하기
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            // 세션 정보에서 ID를 가져온 후 userId를 Set
            return Optional.ofNullable(((AccountDto) authentication.getPrincipal()).getName());
        };
    }
}
