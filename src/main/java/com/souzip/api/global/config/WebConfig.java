package com.souzip.api.global.config;

import com.souzip.api.domain.admin.infrastructure.security.resolver.CurrentAdminIdArgumentResolver;
import com.souzip.api.global.security.resolver.CurrentUserIdArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;
    private final CurrentAdminIdArgumentResolver currentAdminIdArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdArgumentResolver);
        resolvers.add(currentAdminIdArgumentResolver);
    }
}
