package com.club.config;

import com.club.security.JwtAuthenticationFilter;
import com.club.security.SecurityExceptionHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** 是否匿名开放 actuator 监控端点（生产默认 false，仅开发/内网环境开启） */
    @Value("${security.actuator-open:false}")
    private boolean actuatorOpen;

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Resource
    private SecurityExceptionHandler securityExceptionHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/auth/login", "/auth/register", "/auth/refresh", "/auth/captcha").permitAll()
                .requestMatchers("/doc.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(request -> isOpenActuator(request)).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(securityExceptionHandler)
                .accessDeniedHandler(securityExceptionHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * actuator 端点放行策略：仅当 security.actuator-open=true 时匿名开放
     * health/info/prometheus（本地开发与 docker 内网监控场景），否则一律要求认证。
     */
    private boolean isOpenActuator(jakarta.servlet.http.HttpServletRequest request) {
        if (!actuatorOpen) return false;
        // getServletPath() 不含 context-path（/api），与 permitAll 匹配规则一致
        String path = request.getServletPath();
        return path.equals("/actuator/health")
                || path.equals("/actuator/info")
                || path.equals("/actuator/prometheus");
    }
}
