package com.winereviewer.api.controller;

import com.winereviewer.api.repository.UserRepository;
import com.winereviewer.api.security.JwtUtil;
import com.winereviewer.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para autenticação e geração de tokens JWT.
 * <p>
 * Responsável por:
 * - Endpoint de login (mock simplificado para MVP)
 * - Geração de tokens JWT para usuários autenticados
 * <p>
 * <strong>IMPORTANTE - Implementação atual (MVP):</strong>
 * - Login simplificado: busca usuário por email (sem validação de senha)
 * - Apenas para testes e desenvolvimento local
 * - PRODUÇÃO: integrar com Google OAuth (já configurado no application.yml)
 * <p>
 * <strong>Fluxo de autenticação (MVP atual):</strong>
 * <ol>
 *   <li>Cliente envia POST /auth/login com email</li>
 *   <li>Backend busca User no banco por email</li>
 *   <li>Se encontrar, gera JWT com jwtUtil.generateToken(userId)</li>
 *   <li>Retorna JWT para cliente</li>
 *   <li>Cliente guarda JWT e envia em todas as próximas requisições</li>
 * </ol>
 * <p>
 * <strong>Fluxo futuro (com Google OAuth):</strong>
 * <ol>
 *   <li>Cliente envia POST /auth/login com googleIdToken</li>
 *   <li>Backend valida token com Google OAuth API</li>
 *   <li>Backend cria/atualiza User no banco</li>
 *   <li>Backend gera JWT com jwtUtil.generateToken(userId)</li>
 *   <li>Retorna JWT para cliente</li>
 * </ol>
 *
 * @author lucas
 * @date 20/10/2025
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API de autenticação e gerenciamento de tokens JWT")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuthService authService;

    /**
     * Autentica usuário com Google OAuth.
     * <p>
     * <strong>Fluxo de autenticação:</strong>
     * <ol>
     *   <li>Valida Google ID token enviado pelo app</li>
     *   <li>Cria ou atualiza usuário no banco de dados</li>
     *   <li>Gera JWT token para sessão</li>
     *   <li>Retorna JWT + informações do usuário</li>
     * </ol>
     *
     * @param request Google ID token
     * @return JWT token + informações do usuário
     */
    @Operation(
            summary = "Autenticar com Google OAuth",
            description = """
                    Autentica um usuário usando Google OAuth.

                    Fluxo:
                    1. App mobile faz login com Google (google_sign_in package)
                    2. App recebe Google ID token
                    3. App envia token para este endpoint
                    4. Backend valida token com Google
                    5. Backend cria/atualiza usuário
                    6. Backend retorna JWT para uso em requisições subsequentes

                    Use o JWT retornado no header Authorization: Bearer {token}
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticação realizada com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthService.AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token do Google inválido ou expirado"
            )
    })
    @PostMapping("/google")
    public ResponseEntity<AuthService.AuthResponse> authenticateWithGoogle(
            @RequestBody @Valid GoogleAuthRequest request) {
        log.info("Recebida requisição de autenticação Google");

        final var response = authService.authenticateWithGoogle(request.googleIdToken());

        log.info("Autenticação Google concluída com sucesso para usuário: {}", response.email());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de login simplificado (mock para MVP).
     * <p>
     * <strong>DEPRECATED:</strong> Use /auth/google para produção.
     * Este endpoint é apenas para testes de desenvolvimento local.
     * <p>
     * <strong>O que este endpoint faz:</strong>
     * <ol>
     *   <li>Recebe email do usuário</li>
     *   <li>Busca User no banco por email</li>
     *   <li>Se encontrar, gera JWT</li>
     *   <li>Retorna JWT para cliente</li>
     * </ol>
     *
     * @param request dados de login (email)
     * @return JWT token + informações do usuário
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("Recebida requisição de login para email: {}", request.email());

        // 1. Busca usuário no banco por email
        final var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado: {}", request.email());
                    return new IllegalArgumentException("Usuário não encontrado: " + request.email());
                });

        // 2. Gera JWT para o usuário
        final var token = jwtUtil.generateToken(user.getId());

        log.info("Login realizado com sucesso para usuário: {} ({})", user.getDisplayName(), user.getId());

        // 3. Retorna JWT + informações do usuário
        return ResponseEntity.ok(new LoginResponse(
                token,
                user.getId().toString(),
                user.getEmail(),
                user.getDisplayName()
        ));
    }

    // DTOs (inner records)

    /**
     * DTO de requisição para autenticação Google.
     * <p>
     * <strong>Campo:</strong>
     * - googleIdToken: Token ID obtido após login com Google
     * <p>
     * <strong>Validação:</strong>
     * - @NotBlank: não pode ser null, vazio ou apenas espaços
     */
    public record GoogleAuthRequest(
            @NotBlank(message = "Google ID token é obrigatório")
            String googleIdToken
    ) {
    }

    /**
     * DTO de requisição para login.
     * <p>
     * <strong>DEPRECATED:</strong> Use GoogleAuthRequest para produção.
     * <p>
     * <strong>Campos:</strong>
     * - email: Email do usuário (obrigatório, deve ser válido)
     * <p>
     * <strong>Validações:</strong>
     * - @NotBlank: não pode ser null, vazio ou apenas espaços
     * - @Email: deve ser um email válido
     */
    public record LoginRequest(
            @NotBlank(message = "Email é obrigatório")
            @Email(message = "Email inválido")
            String email
    ) {
    }

    /**
     * DTO de resposta para login.
     * <p>
     * <strong>Campos:</strong>
     * - token: JWT gerado (usar no header Authorization: Bearer {token})
     * - userId: UUID do usuário (para referência)
     * - email: Email do usuário
     * - displayName: Nome de exibição do usuário
     * <p>
     * <strong>Como usar o token:</strong>
     * <pre>
     * Authorization: Bearer {token}
     * </pre>
     */
    public record LoginResponse(
            String token,
            String userId,
            String email,
            String displayName
    ) {
    }

}
