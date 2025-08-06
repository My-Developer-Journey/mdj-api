package com.diemyolo.blog_api.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {
        private final List<String> ALLOWED_METHODS = List.of(
                        "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD");
        private final List<String> ALLOWED_HEADERS = List.of("Authorization", "Content-Type");
        private final List<String> ALLOWED_ORIGINS;

        private final AuthenticationProvider authenticationProvider;
        private final JWTAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfiguration(
                        @Value("${app.frontend.origin}") String frontendOrigin,
                        AuthenticationProvider authenticationProvider,
                        JWTAuthenticationFilter jwtAuthenticationFilter) {
                this.ALLOWED_ORIGINS = List.of(frontendOrigin);
                this.authenticationProvider = authenticationProvider;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .formLogin(form -> form.disable())
                                .httpBasic(basic -> basic.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-resources/**",
                                                                "/webjars/**",
                                                                "/swagger-ui.html",
                                                                "/favicon.ico",
                                                                "/v2/api-docs",
                                                                "/")
                                                .permitAll()
                                                .requestMatchers(
                                                                "/api/authentications/sign-in",
                                                                "/api/authentications/sign-up",
                                                                "/api/authentications/verify")
                                                .permitAll()
                                                .requestMatchers(
                                                                "/api/awss3/upload",
                                                                "/api/awss3/test")
                                                .permitAll()
                                                .requestMatchers(
                                                                "/api/admin/**",
                                                                "/api/categories/**")
                                                .hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(ALLOWED_ORIGINS);
                configuration.setAllowedMethods(ALLOWED_METHODS);
                configuration.setAllowedHeaders(ALLOWED_HEADERS);
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
