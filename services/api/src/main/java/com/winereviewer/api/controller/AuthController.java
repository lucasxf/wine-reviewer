package com.winereviewer.api.controller;

import com.winereviewer.api.repository.UserRepository;
import com.winereviewer.api.security.JwtUtil;
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
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * Endpoint de login simplificado (mock para MVP).
     * <p>
     * <strong>O que este endpoint faz:</strong>
     * <ol>
     *   <li>Recebe email do usuário</li>
     *   <li>Busca User no banco por email</li>
     *   <li>Se encontrar, gera JWT</li>
     *   <li>Retorna JWT para cliente</li>
     * </ol>
     * <p>
     * <strong>ATENÇÃO - Implementação simplificada:</strong>
     * - Não valida senha (não temos senha, é OAuth)
     * - Apenas para testes de desenvolvimento
     * - PRODUÇÃO: substituir por Google OAuth
     * <p>
     * <strong>Como testar (Postman/curl):</strong>
     * <pre>
     * POST http://localhost:8080/auth/login
     * Content-Type: application/json
     *
     * {
     *   "email": "user@example.com"
     * }
     *
     * Response 200 OK:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "userId": "123e4567-e89b-12d3-a456-426614174000",
     *   "email": "user@example.com"
     * }
     * </pre>
     * <p>
     * <strong>Usar o token em outras requisições:</strong>
     * <pre>
     * POST http://localhost:8080/reviews
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * Content-Type: application/json
     *
     * {
     *   "wineId": "...",
     *   "rating": 5,
     *   "notes": "Excelente!"
     * }
     * </pre>
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
     * DTO de requisição para login.
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
