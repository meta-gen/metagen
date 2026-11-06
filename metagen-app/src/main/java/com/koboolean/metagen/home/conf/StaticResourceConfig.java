package com.koboolean.metagen.home.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * 정적 리소스 캐시 설정
 * <p>
 * Spring Security 는 모든 응답에 {@code Cache-Control: no-cache, no-store} 를 붙이므로
 * 페이지를 이동할 때마다 폰트(약 2MB)를 다시 받아 기본 폰트 → Pretendard 로 바뀌며 화면이 깜빡였다.
 * 애플리케이션이 Cache-Control 을 먼저 지정하면 Spring Security 는 덮어쓰지 않는다.
 * <ul>
 *     <li>폰트 : 파일이 바뀌지 않으므로 1년 캐시 (immutable)</li>
 *     <li>이미지 : 7일 캐시</li>
 *     <li>CSS / JS : 캐시는 하되 매번 변경 여부 확인(no-cache → 304) — 배포 후 바로 반영</li>
 * </ul>
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/fonts/**")
                .addResourceLocations("classpath:/static/fonts/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCacheControl(CacheControl.noCache());

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCacheControl(CacheControl.noCache());
    }
}
