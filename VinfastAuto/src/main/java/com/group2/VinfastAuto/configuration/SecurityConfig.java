package com.group2.VinfastAuto.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // De dung duoc @Pre/@PostAuthorize
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {"/auth/**", "/users"};
    private static final String[] PUBLIC_RESOURCES = {"/public_resources/**", "/libs/**"};
    private static final String[] PRIVATE_RESOURCES = {"/private_resources/**"};
    private static final String[] LOGIN_RESOURCES = {"/"};

    CustomJwtDecoder customJwtDecoder;
    CorsConfigurationSource corsConfigurationSource;

    @Autowired
    public SecurityConfig(CustomJwtDecoder customJwtDecoder, CorsConfigurationSource corsConfigurationSource) {
        this.customJwtDecoder = customJwtDecoder;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_RESOURCES).permitAll()
                        .requestMatchers(HttpMethod.GET, LOGIN_RESOURCES).permitAll()
//                        .requestMatchers(HttpMethod.GET, PRIVATE_RESOURCES).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, PRIVATE_RESOURCES).permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(configurer -> configurer
                     .jwt(jwtConfigurer -> jwtConfigurer
                         .decoder(customJwtDecoder)  // Verify token
                         .jwtAuthenticationConverter(jwtAuthenticationConverter()) // Convert token scope

                     )
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                )

                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(new JwtAccessDeniedHandler())
                )
        ;

        httpSecurity.csrf(abstractHttpConfigurer -> abstractHttpConfigurer.disable());

        return httpSecurity.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }



}
