package com.weeTalk.weeTalk.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private SessionAuthenticationFilter sessionAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(authz -> authz
                // 인증이 필요 없는 공개 API
                .requestMatchers("/api/users/signup").permitAll()
                .requestMatchers("/api/users/login").permitAll()
                .requestMatchers("/api/users/validate-token").permitAll()
                .requestMatchers("/api/users/send-verification").permitAll()
                .requestMatchers("/api/users/verify-code").permitAll()
                .requestMatchers("/api/users/check-email").permitAll()
                .requestMatchers("/api/users/check-nickname").permitAll()
                .requestMatchers("/api/users/check-weetalk-id").permitAll()
                .requestMatchers("/api/users/create-weetalk-id").permitAll()
                .requestMatchers("/api/users/validate-user").permitAll()
                .requestMatchers("/api/users/search-by-weetalk-id").permitAll()
                .requestMatchers("/api/users/add-friend").permitAll()
                .requestMatchers("/api/users/**").permitAll()
                .requestMatchers("/api/groups/**").permitAll()
                .requestMatchers("/api/chat/**").permitAll()
                // 인증이 필요한 보호된 API
                .anyRequest().authenticated()
            )
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
