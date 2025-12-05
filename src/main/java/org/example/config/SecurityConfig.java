package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // enable CORS support so Spring Security honors WebMvc CORS config
                .csrf(csrf -> csrf.disable()) // disable if using API + JWT
                .authorizeHttpRequests(auth -> auth
                        // 👇 Public endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // allow preflight
                        .requestMatchers("/product/**").permitAll()
                        .requestMatchers("/review/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/images/**").permitAll()


                        // 👇 Everything else requires authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
