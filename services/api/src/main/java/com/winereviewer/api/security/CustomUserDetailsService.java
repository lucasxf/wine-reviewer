package com.winereviewer.api.security;

import com.winereviewer.api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

/**
 * Implementação customizada de UserDetailsService para carregar usuários do banco.
 * <p>
 * <strong>O que este serviço faz:</strong>
 * <ul>
 *   <li>Implementa a interface {@link UserDetailsService} do Spring Security</li>
 *   <li>Carrega dados do usuário do banco de dados via {@link UserRepository}</li>
 *   <li>Converte {@link com.winereviewer.api.domain.User} → {@link UserDetails}</li>
 *   <li>Usado pelo {@link JwtAuthenticationFilter} após validar o token JWT</li>
 * </ul>
 * <p>
 * <strong>Por que precisa disso:</strong>
 * - O Spring Security exige um UserDetailsService para carregar usuários
 * - Quando o JWT é validado, precisamos buscar o User completo do banco
 * - O UserDetails é armazenado no SecurityContext (por isso @AuthenticationPrincipal funciona)
 * <p>
 * <strong>Fluxo de autenticação:</strong>
 * <ol>
 *   <li>Request chega com header "Authorization: Bearer {token}"</li>
 *   <li>JwtAuthenticationFilter valida token e extrai userId</li>
 *   <li>JwtAuthenticationFilter chama loadUserByUsername(userId)</li>
 *   <li>Este serviço busca User no banco via UserRepository</li>
 *   <li>Converte para UserDetails e retorna</li>
 *   <li>Spring Security armazena UserDetails no SecurityContext</li>
 *   <li>Controller acessa via @AuthenticationPrincipal UserDetails</li>
 * </ol>
 *
 * @author lucas
 * @date 20/10/2025
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Construtor com injeção de UserRepository.
     *
     * @param userRepository repositório para buscar usuários no banco
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Carrega um usuário pelo "username" (no nosso caso, o userId em formato String).
     * <p>
     * <strong>Importante sobre o parâmetro "username":</strong>
     * - A interface UserDetailsService exige o método loadUserByUsername(String username)
     * - No nosso caso, não usamos username tradicional, mas sim UUID
     * - Por isso, o parâmetro "username" na verdade é o userId em formato String
     * - Exemplo: username = "123e4567-e89b-12d3-a456-426614174000"
     * <p>
     * <strong>O que este método faz:</strong>
     * <ol>
     *   <li>Recebe userId como String</li>
     *   <li>Converte String → UUID</li>
     *   <li>Busca User no banco via UserRepository.findById()</li>
     *   <li>Se não encontrar, lança UsernameNotFoundException (401 Unauthorized)</li>
     *   <li>Converte User entity → UserDetails (usando builder do Spring Security)</li>
     *   <li>Retorna UserDetails com userId no campo "username"</li>
     * </ol>
     * <p>
     * <strong>Estrutura do UserDetails retornado:</strong>
     * <pre>
     * UserDetails {
     *   username: "123e4567-e89b-12d3-a456-426614174000"  ← userId (String)
     *   password: ""                                       ← Vazio (não usamos senha, JWT já validou)
     *   authorities: []                                    ← Vazio por enquanto (futuramente: roles)
     *   accountNonExpired: true
     *   accountNonLocked: true
     *   credentialsNonExpired: true
     *   enabled: true
     * }
     * </pre>
     * <p>
     * <strong>Por que password está vazio:</strong>
     * - Não usamos senha neste projeto (autenticação via Google OAuth)
     * - O JWT já foi validado antes de chamar este método
     * - O campo password é obrigatório na interface, mas não é usado
     * <p>
     * <strong>Por que authorities está vazio:</strong>
     * - Por enquanto não implementamos roles/permissões
     * - Futuramente: podemos adicionar roles (ROLE_USER, ROLE_ADMIN)
     * - Collections.emptyList() é suficiente por agora
     *
     * @param username userId em formato String (ex: "123e4567-e89b-12d3-a456-426614174000")
     * @return UserDetails contendo dados do usuário
     * @throws UsernameNotFoundException se usuário não for encontrado no banco
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Carregando usuário com userId: {}", username);

        // 1. Converte String → UUID
        final UUID userId;
        try {
            userId = UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            log.warn("UserId inválido no token: {}", username);
            throw new UsernameNotFoundException("UserId inválido: " + username);
        }

        // 2. Busca User no banco
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado no banco: {}", userId);
                    return new UsernameNotFoundException("Usuário não encontrado: " + userId);
                });

        log.debug("Usuário carregado com sucesso: {} ({})", user.getDisplayName(), user.getEmail());

        // 3. Converte User entity → UserDetails
        return toUserDetails(user);
    }

    // Private helper methods (ordenados por invocação)

    /**
     * Converte User entity para UserDetails do Spring Security.
     * <p>
     * <strong>O que este método faz:</strong>
     * - Usa o builder User.builder() do Spring Security (não confundir com nossa entity User!)
     * - Mapeia userId → username (campo obrigatório do UserDetails)
     * - Define password como vazio (não usamos senha)
     * - Define authorities como lista vazia (sem roles por enquanto)
     * - Define todas as flags como true (conta ativa, não expirada, etc.)
     * <p>
     * <strong>Por que usar User.builder():</strong>
     * - É a forma recomendada pelo Spring Security para criar UserDetails
     * - Retorna um UserDetails imutável (thread-safe)
     * - Mais limpo que implementar UserDetails manualmente
     * <p>
     * <strong>Atenção - Duas classes "User":</strong>
     * - com.winereviewer.api.domain.User → Nossa entity do banco
     * - org.springframework.security.core.userdetails.User → Builder do Spring Security
     * - Por isso o import explícito no topo do arquivo!
     *
     * @param user nossa entity User do banco de dados
     * @return UserDetails do Spring Security
     */
    private UserDetails toUserDetails(com.winereviewer.api.domain.User user) {
        return User.builder()
                .username(user.getId().toString())  // ← userId no campo "username"
                .password("")                        // ← Vazio (JWT já validou, não precisamos de senha)
                .authorities(Collections.emptyList()) // ← Sem roles por enquanto
                .accountExpired(false)               // ← Conta não expirada
                .accountLocked(false)                // ← Conta não bloqueada
                .credentialsExpired(false)           // ← Credenciais não expiradas
                .disabled(false)                     // ← Conta ativa
                .build();
    }

}
