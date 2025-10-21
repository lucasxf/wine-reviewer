package com.winereviewer.api.config;

import com.winereviewer.api.security.JwtAuthenticationFilter;
import com.winereviewer.api.security.JwtUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração de segurança da API.
 * <p>
 * Responsável por:
 * - Habilitar JwtProperties (@EnableConfigurationProperties)
 * - Configurar Spring Security (CORS, CSRF, session management)
 * - Adicionar JwtAuthenticationFilter na cadeia de filtros
 * - Definir endpoints públicos vs protegidos
 * <p>
 * <strong>Ordem dos filtros:</strong>
 * <pre>
 * Request → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter → ... → Controller
 * </pre>
 *
 * @author lucas
 * @date 20/10/2025
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class, GoogleOAuthProperties.class})
public class SecurityConfig {

    /**
     * Cria o filtro de autenticação JWT como um bean gerenciado pelo Spring.
     * <p>
     * <strong>Por que @Bean ao invés de @Component:</strong>
     * - Controle explícito: fica claro que o filtro é parte da configuração de segurança
     * - Facilita testes: pode ser facilmente mockado em testes de controllers
     * - Segue padrão Spring Security: configurações devem declarar seus beans
     * - Evita registro duplo: filtro só é criado quando necessário
     *
     * @param jwtUtil utilitário para validação de JWT
     * @param userDetailsService serviço para carregar usuários do banco
     * @return filtro de autenticação JWT configurado
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    /**
     * Configura a cadeia de filtros de segurança.
     * <p>
     * <strong>O que este método faz:</strong>
     * <ol>
     *   <li>Desabilita CSRF (não necessário para APIs stateless com JWT)</li>
     *   <li>Configura CORS (permite requests do frontend)</li>
     *   <li>Define session management como STATELESS (não cria sessões HTTP)</li>
     *   <li>Adiciona JwtAuthenticationFilter ANTES do UsernamePasswordAuthenticationFilter</li>
     *   <li>Define endpoints públicos (não precisam autenticação)</li>
     *   <li>Define que todos os outros endpoints precisam autenticação</li>
     * </ol>
     * <p>
     * <strong>Por que CSRF desabilitado:</strong>
     * - CSRF protection é necessário para apps com sessões (cookies)
     * - Nossa API é stateless (usa JWT no header, não cookies)
     * - CSRF não protege contra ataques via header Authorization
     * - Por isso é seguro desabilitar CSRF em APIs JWT
     * <p>
     * <strong>Por que STATELESS:</strong>
     * - Não queremos que Spring Security crie sessões HTTP (jsessionid)
     * - JWT é stateless: todas as informações estão no token
     * - Servidor não guarda estado de autenticação em memória
     * - Cada request é independente (precisa enviar JWT)
     * <p>
     * <strong>Por que addFilterBefore:</strong>
     * - JwtAuthenticationFilter precisa executar ANTES dos filtros padrão
     * - UsernamePasswordAuthenticationFilter é um filtro padrão do Spring Security
     * - Queremos validar JWT antes que Spring Security tente validar username/password
     * - addFilterBefore garante a ordem correta de execução
     * <p>
     * <strong>Endpoints públicos (não precisam JWT):</strong>
     * - /actuator/** (health checks, metrics)
     * - /api-docs/** (OpenAPI/Swagger docs)
     * - /swagger-ui/** (Swagger UI)
     * - /auth/login (endpoint de login para gerar JWT - será criado)
     * <p>
     * <strong>Endpoints protegidos (precisam JWT):</strong>
     * - Todos os outros (.anyRequest().authenticated())
     * - Exemplos: POST /reviews, PUT /reviews/{id}, DELETE /reviews/{id}
     *
     * @param http objeto HttpSecurity do Spring Security
     * @return SecurityFilterChain configurado
     * @throws Exception se erro na configuração
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // 1. Desabilita CSRF (não necessário para APIs stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configura CORS (permite requests do frontend)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Session management STATELESS (não cria sessões HTTP)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Adiciona JwtAuthenticationFilter ANTES do UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 5. Define regras de autorização
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (não precisam autenticação)
                        .requestMatchers(
                                "/actuator/**",      // Health checks, metrics
                                "/api-docs/**",      // OpenAPI docs
                                "/swagger-ui/**",    // Swagger UI
                                "/swagger-ui.html",  // Swagger UI HTML
                                "/auth/**"           // Login endpoint (será criado)
                        ).permitAll()

                        // Todos os outros endpoints precisam autenticação
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8080"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
