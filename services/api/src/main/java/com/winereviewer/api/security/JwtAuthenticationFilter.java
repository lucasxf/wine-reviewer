package com.winereviewer.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro de autenticação JWT que intercepta todas as requisições HTTP.
 * <p>
 * <strong>O que este filtro faz:</strong>
 * <ul>
 *   <li>Intercepta TODAS as requisições antes de chegarem aos controllers</li>
 *   <li>Extrai token JWT do header "Authorization: Bearer {token}"</li>
 *   <li>Valida o token (assinatura + expiração)</li>
 *   <li>Carrega UserDetails do banco via {@link CustomUserDetailsService}</li>
 *   <li>Armazena UserDetails no {@link SecurityContextHolder} (SecurityContext)</li>
 *   <li>Permite que requisição continue (Spring Security decidirá se bloqueia ou não)</li>
 * </ul>
 * <p>
 * <strong>Por que estender OncePerRequestFilter:</strong>
 * - OncePerRequestFilter garante que o filtro executa APENAS UMA VEZ por request
 * - Mesmo se houver forwards/includes internos, não processa novamente
 * - É a classe base recomendada para filtros customizados no Spring
 * <p>
 * <strong>Fluxo de autenticação (passo a passo):</strong>
 * <ol>
 *   <li>Request chega: GET /reviews/123 com header "Authorization: Bearer eyJhbGci..."</li>
 *   <li>Este filtro intercepta ANTES do controller</li>
 *   <li>Extrai token do header com extractTokenFromHeader()</li>
 *   <li>Valida token com jwtUtil.validateToken()</li>
 *   <li>Se válido, extrai userId com jwtUtil.extractUserId()</li>
 *   <li>Carrega UserDetails do banco com userDetailsService.loadUserByUsername(userId)</li>
 *   <li>Cria Authentication com UserDetails + roles</li>
 *   <li>Armazena no SecurityContext com SecurityContextHolder.getContext().setAuthentication()</li>
 *   <li>Permite que request continue (filterChain.doFilter())</li>
 *   <li>Controller recebe request com @AuthenticationPrincipal funcionando</li>
 * </ol>
 * <p>
 * <strong>O que acontece se token for inválido:</strong>
 * - Filtro NÃO lança exceção (não bloqueia request)
 * - Simplesmente não autentica (SecurityContext fica vazio)
 * - Spring Security verifica se endpoint precisa autenticação
 * - Se precisar, retorna 401 Unauthorized
 * - Se não precisar (permitAll), permite acesso
 * <p>
 * <strong>Ordem de execução dos filtros:</strong>
 * <pre>
 * Request → JwtAuthenticationFilter → (outros filtros Spring Security) → Controller
 * </pre>
 *
 * @author lucas
 * @date 20/10/2025
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Construtor com injeção de dependências.
     *
     * @param jwtUtil             utilitário para validar e extrair dados do JWT
     * @param userDetailsService  serviço para carregar User do banco (CustomUserDetailsService)
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Método principal do filtro, executado uma vez por requisição.
     * <p>
     * <strong>O que este método faz (passo a passo):</strong>
     * <ol>
     *   <li>Extrai token do header "Authorization"</li>
     *   <li>Se não houver token, passa adiante (request sem autenticação)</li>
     *   <li>Se houver token, valida com jwtUtil.validateToken()</li>
     *   <li>Se inválido, passa adiante (request sem autenticação)</li>
     *   <li>Se válido, extrai userId e carrega UserDetails</li>
     *   <li>Autentica usuário no SecurityContext</li>
     *   <li>Passa request adiante na chain (filterChain.doFilter)</li>
     * </ol>
     * <p>
     * <strong>Por que não lançamos exceção se token inválido:</strong>
     * - Alguns endpoints são públicos (não precisam autenticação)
     * - Exemplo: GET /swagger-ui.html, GET /actuator/health
     * - Se lançássemos exceção, esses endpoints não funcionariam
     * - O Spring Security decidirá se endpoint precisa auth ou não
     * <p>
     * <strong>Atenção - SecurityContext por thread:</strong>
     * - SecurityContextHolder usa ThreadLocal (uma cópia por thread)
     * - Cada requisição é processada por uma thread diferente
     * - Por isso não há "vazamento" de auth entre requests diferentes
     *
     * @param request     requisição HTTP
     * @param response    resposta HTTP
     * @param filterChain cadeia de filtros do Spring Security
     * @throws ServletException se erro de servlet
     * @throws IOException      se erro de I/O
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Extrai token do header "Authorization: Bearer {token}"
        final String token = extractTokenFromHeader(request);

        // 2. Se não houver token, passa adiante sem autenticar
        if (token == null) {
            log.debug("Nenhum token JWT encontrado no header Authorization");
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Valida token (assinatura + expiração)
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token JWT inválido ou expirado");
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Extrai userId do token
        final UUID userId;
        try {
            userId = jwtUtil.extractUserId(token);
        } catch (Exception e) {
            log.warn("Erro ao extrair userId do token: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Verifica se já está autenticado (evita reprocessamento desnecessário)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("Usuário já autenticado no SecurityContext, pulando autenticação JWT");
            filterChain.doFilter(request, response);
            return;
        }

        // 6. Carrega UserDetails do banco
        try {
            authenticateUser(userId, request);
            log.debug("Usuário autenticado com sucesso via JWT: {}", userId);
        } catch (Exception e) {
            log.error("Erro ao autenticar usuário: {}", e.getMessage(), e);
            // Não lança exceção, apenas continua sem autenticar
        }

        // 7. Passa request adiante na chain (vai para próximo filtro ou controller)
        filterChain.doFilter(request, response);
    }

    // Private helper methods (ordenados por invocação)

    /**
     * Extrai token JWT do header "Authorization".
     * <p>
     * <strong>Formato esperado do header:</strong>
     * <pre>
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * </pre>
     * <p>
     * <strong>O que este método faz:</strong>
     * <ol>
     *   <li>Lê header "Authorization" do request</li>
     *   <li>Verifica se começa com "Bearer " (com espaço)</li>
     *   <li>Se sim, remove "Bearer " e retorna apenas o token</li>
     *   <li>Se não, retorna null (sem token)</li>
     * </ol>
     * <p>
     * <strong>Exemplos:</strong>
     * <pre>
     * Header: "Bearer abc123"  → Retorna: "abc123"
     * Header: "abc123"         → Retorna: null (falta "Bearer ")
     * Header: null             → Retorna: null
     * Header: ""               → Retorna: null
     * </pre>
     * <p>
     * <strong>Por que exige "Bearer ":</strong>
     * - É o padrão RFC 6750 (OAuth 2.0 Bearer Token)
     * - Indica que o token é do tipo "bearer" (portador)
     * - Separa de outros tipos de auth (Basic, Digest, etc.)
     *
     * @param request requisição HTTP
     * @return token JWT (sem "Bearer ") ou null se não encontrar
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        // Verifica se header existe e começa com "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Remove "Bearer " (7 caracteres) e retorna apenas o token
            return authHeader.substring(7);
        }

        return null;
    }

    /**
     * Autentica usuário no SecurityContext.
     * <p>
     * <strong>O que este método faz (passo a passo):</strong>
     * <ol>
     *   <li>Carrega UserDetails do banco via userDetailsService.loadUserByUsername(userId)</li>
     *   <li>Cria objeto Authentication (UsernamePasswordAuthenticationToken)</li>
     *   <li>Adiciona detalhes da requisição (IP, session, etc.)</li>
     *   <li>Armazena Authentication no SecurityContext</li>
     * </ol>
     * <p>
     * <strong>Por que usar UsernamePasswordAuthenticationToken:</strong>
     * - É a implementação padrão de Authentication no Spring Security
     * - Apesar do nome "UsernamePassword", funciona para qualquer tipo de auth
     * - No nosso caso: username = userId, password = vazio (JWT já validou)
     * <p>
     * <strong>O que é Authentication:</strong>
     * - Interface do Spring Security que representa um usuário autenticado
     * - Contém: UserDetails (principal), credentials (password), authorities (roles)
     * - É armazenado no SecurityContext (ThreadLocal)
     * <p>
     * <strong>Como @AuthenticationPrincipal funciona:</strong>
     * <pre>
     * 1. Este método armazena Authentication no SecurityContext
     * 2. Spring Security lê SecurityContext quando vê @AuthenticationPrincipal
     * 3. Extrai UserDetails do Authentication.getPrincipal()
     * 4. Injeta no parâmetro do controller
     * </pre>
     * <p>
     * <strong>Por que setDetails(...):</strong>
     * - Adiciona informações extras da requisição (IP, session ID, etc.)
     * - Útil para auditoria e logs
     * - Não é obrigatório, mas é boa prática
     *
     * @param userId  UUID do usuário extraído do JWT
     * @param request requisição HTTP (para adicionar detalhes)
     */
    private void authenticateUser(UUID userId, HttpServletRequest request) {
        // 1. Carrega UserDetails do banco
        final UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());

        // 2. Cria objeto Authentication
        // Parâmetros: principal (UserDetails), credentials (null), authorities (roles)
        final var authentication = new UsernamePasswordAuthenticationToken(
                userDetails,           // ← Principal (quem é o usuário)
                null,                  // ← Credentials (senha, mas já validamos via JWT)
                userDetails.getAuthorities()  // ← Roles/permissões (ex: ROLE_USER, ROLE_ADMIN)
        );

        // 3. Adiciona detalhes da requisição (IP, session, etc.)
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 4. Armazena no SecurityContext (ThreadLocal)
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
