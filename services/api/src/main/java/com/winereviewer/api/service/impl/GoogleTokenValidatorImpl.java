package com.winereviewer.api.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.winereviewer.api.config.GoogleOAuthProperties;
import com.winereviewer.api.exception.InvalidTokenException;
import com.winereviewer.api.service.GoogleTokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Implementação do serviço de validação de tokens do Google.
 * <p>
 * Utiliza a biblioteca oficial do Google para:
 * - Verificar assinatura do token com chaves públicas do Google
 * - Validar expiração
 * - Validar audience (client ID)
 *
 * @author lucas
 * @date 21/10/2025
 */
@Slf4j
@Service
public class GoogleTokenValidatorImpl implements GoogleTokenValidator {

    private final GoogleIdTokenVerifier verifier;

    /**
     * Construtor com injeção de GoogleOAuthProperties.
     * <p>
     * Inicializa o GoogleIdTokenVerifier com:
     * - Transport HTTP para buscar chaves públicas do Google
     * - JSON Factory para parsing
     * - Client ID para validação de audience
     *
     * @param properties configurações Google OAuth carregadas do application.yml
     */
    public GoogleTokenValidatorImpl(GoogleOAuthProperties properties) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(properties.getClientId()))
                .build();

        log.info("GoogleTokenValidator inicializado com clientId: {}", maskClientId(properties.getClientId()));
    }

    @Override
    public GoogleUserInfo validateToken(String idToken) {
        try {
            log.debug("Validando Google ID token...");

            // 1. Verifica assinatura, expiração e audience
            final GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                throw new InvalidTokenException("Token inválido ou expirado");
            }

            // 2. Extrai payload do token
            final GoogleIdToken.Payload payload = googleIdToken.getPayload();

            // 3. Extrai informações do usuário
            final String googleId = payload.getSubject(); // Google user ID (sub claim)
            final String email = payload.getEmail();
            final String name = (String) payload.get("name");
            final String picture = (String) payload.get("picture");

            log.info("Token validado com sucesso. Email: {}, GoogleId: {}", email, googleId);

            return new GoogleUserInfo(googleId, email, name, picture);

        } catch (Exception e) {
            log.error("Erro ao validar token do Google: {}", e.getMessage());
            throw new InvalidTokenException("Falha ao validar token do Google: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    /**
     * Mascara o client ID para logs (mostra apenas primeiros e últimos 8 caracteres).
     *
     * @param clientId client ID completo
     * @return client ID mascarado (ex: "12345678...xyz12345")
     */
    private String maskClientId(String clientId) {
        if (clientId == null || clientId.length() < 16) {
            return "***";
        }
        return clientId.substring(0, 8) + "..." + clientId.substring(clientId.length() - 8);
    }

}
