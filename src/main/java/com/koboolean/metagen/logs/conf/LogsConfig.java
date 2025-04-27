package com.koboolean.metagen.logs.conf;

import com.koboolean.metagen.logs.interceptor.LogsInterceptor;
import com.koboolean.metagen.logs.repository.LogsRepository;
import com.koboolean.metagen.logs.service.LogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class LogsConfig implements WebMvcConfigurer {

    private final LogsService logsService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogsInterceptor(logsService))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/updatePwd", "/api/**/data", "/api/**/column", "/api/selectCodeRule/detail");
    }
}
