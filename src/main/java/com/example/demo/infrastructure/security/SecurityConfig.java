package com.example.demo.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
            // 1. Proteção CSRF com repositório em Cookie e filtro de forçamento
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers("/login", "/logout")
            )
            // ADICIONADO: Força a persistência do cookie XSRF-TOKEN no navegador
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
            
            // 2. Tratamento de exceções
            .exceptionHandling(exception -> exception
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")
                )
            )

            // 3. Regras de Autorização de Rotas
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/css/**",
                    "/js/**",
                    "/assets/**",
                    "/favicon.ico",
                    "/login",
                    "/logout",
                    "/register",
                    "/register.html"
                ).permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )

            // 4. Fluxo de Autenticação
            .formLogin(form -> form
                .loginPage("/index.html")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    response.setStatus(HttpStatus.OK.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"status\": \"sucesso\", \"mensagem\": \"Login realizado com sucesso!\"}");
                })
                .failureHandler((request, response, exception) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"status\": \"erro\", \"mensagem\": \"" + exception.getMessage() + "\"}");
                })
            )

            // 5. Configuração de Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpStatus.OK.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"status\": \"sucesso\", \"mensagem\": \"Logout efetuado com sucesso!\"}");
                })
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Filtro para garantir a criação e atualização do cookie XSRF-TOKEN
    private static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                // Ao invocar o getToken(), forçamos a inicialização do token preguiçoso (deferred token)
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }
}