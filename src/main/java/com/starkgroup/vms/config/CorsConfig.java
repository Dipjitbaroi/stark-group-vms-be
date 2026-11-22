package com.starkgroup.vms.config;

import com.starkgroup.vms.security.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;
    private final JwtAuthInterceptor jwtAuthInterceptor;

    public CorsConfig(
            @Value("${vms.cors.allowed-origins}") String allowedOrigins,
            JwtAuthInterceptor jwtAuthInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            this.allowedOrigins = new String[]{"*"};
        } else {
            this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Protect all vehicle domain endpoints; keep login, root, and errors public
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/vehicles", "/api/vehicles/**")
                .excludePathPatterns("/api/auth/**", "/", "/error");
    }
}
