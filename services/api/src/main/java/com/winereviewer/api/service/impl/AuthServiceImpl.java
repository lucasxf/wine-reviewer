package com.winereviewer.api.service.impl;

import com.winereviewer.api.domain.User;
import com.winereviewer.api.repository.UserRepository;
import com.winereviewer.api.security.JwtUtil;
import com.winereviewer.api.service.AuthService;
import com.winereviewer.api.service.GoogleTokenValidator;
import com.winereviewer.api.service.GoogleTokenValidator.GoogleUserInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Implementação do serviço de autenticação.
 *
 * @author lucas
 * @date 21/10/2025
 */
@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final GoogleTokenValidator googleTokenValidator;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse authenticateWithGoogle(String googleIdToken) {
        log.info("Iniciando autenticação com Google OAuth");

        // 1. Valida token do Google e extrai informações do usuário
        final GoogleUserInfo googleUserInfo = googleTokenValidator.validateToken(googleIdToken);

        log.info("Token Google validado. Email: {}, GoogleId: {}",
                googleUserInfo.email(), googleUserInfo.googleId());

        // 2. Busca usuário existente por googleId ou cria novo
        final User user = findOrCreateUser(googleUserInfo);

        // 3. Gera JWT token para o usuário
        final String jwtToken = jwtUtil.generateToken(user.getId());

        log.info("Autenticação concluída com sucesso. UserId: {}, Email: {}",
                user.getId(), user.getEmail());

        // 4. Retorna resposta com token e informações do usuário
        return new AuthResponse(
                jwtToken,
                user.getId().toString(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl()
        );
    }

    // Private helper methods (ordered by invocation flow)

    /**
     * Busca usuário existente por googleId ou cria novo.
     * <p>
     * Se usuário já existe, atualiza informações (nome, email, avatar).
     * Se não existe, cria novo usuário com dados do Google.
     *
     * @param googleUserInfo informações do usuário extraídas do Google token
     * @return User encontrado ou criado
     */
    private User findOrCreateUser(GoogleUserInfo googleUserInfo) {
        return userRepository.findByGoogleId(googleUserInfo.googleId())
                .map(existingUser -> updateUserInfo(existingUser, googleUserInfo))
                .orElseGet(() -> createNewUser(googleUserInfo));
    }

    /**
     * Atualiza informações de um usuário existente.
     * <p>
     * Atualiza apenas se houver mudanças:
     * - Display name
     * - Email
     * - Avatar URL
     *
     * @param user           usuário existente
     * @param googleUserInfo novas informações do Google
     * @return usuário atualizado
     */
    private User updateUserInfo(User user, GoogleUserInfo googleUserInfo) {
        log.info("Usuário já existe. Atualizando informações. UserId: {}", user.getId());

        boolean hasChanges = false;

        // Atualiza display name se mudou
        if (!user.getDisplayName().equals(googleUserInfo.name())) {
            user.setDisplayName(googleUserInfo.name());
            hasChanges = true;
        }

        // Atualiza email se mudou
        if (!user.getEmail().equals(googleUserInfo.email())) {
            user.setEmail(googleUserInfo.email());
            hasChanges = true;
        }

        // Atualiza avatar URL se mudou
        if (googleUserInfo.picture() != null && !googleUserInfo.picture().equals(user.getAvatarUrl())) {
            user.setAvatarUrl(googleUserInfo.picture());
            hasChanges = true;
        }

        if (hasChanges) {
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            log.info("Informações do usuário atualizadas. UserId: {}", user.getId());
        } else {
            log.debug("Nenhuma alteração necessária para usuário: {}", user.getId());
        }

        return user;
    }

    /**
     * Cria um novo usuário com informações do Google.
     *
     * @param googleUserInfo informações do usuário do Google
     * @return novo usuário criado
     */
    private User createNewUser(GoogleUserInfo googleUserInfo) {
        log.info("Criando novo usuário. Email: {}, GoogleId: {}",
                googleUserInfo.email(), googleUserInfo.googleId());

        final var newUser = new User();
        newUser.setGoogleId(googleUserInfo.googleId());
        newUser.setEmail(googleUserInfo.email());
        newUser.setDisplayName(googleUserInfo.name());
        newUser.setAvatarUrl(googleUserInfo.picture());

        final var savedUser = userRepository.save(newUser);

        log.info("Novo usuário criado com sucesso. UserId: {}", savedUser.getId());

        return savedUser;
    }

}
