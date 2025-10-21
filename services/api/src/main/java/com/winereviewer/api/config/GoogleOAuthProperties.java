package com.winereviewer.api.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração para Google OAuth.
 * <p>
 * Carrega configurações do application.yml com prefix "google.oauth".
 * <p>
 * Exemplo de application.yml:
 * <pre>
 * google:
 *   oauth:
 *     client-id: your-google-client-id.apps.googleusercontent.com
 * </pre>
 *
 * @author lucas
 * @date 21/10/2025
 */
@Getter
@ConfigurationProperties(prefix = "google.oauth")
public class GoogleOAuthProperties {

    /**
     * Google OAuth Client ID.
     * <p>
     * Obtido no Google Cloud Console:
     * 1. Acesse https://console.cloud.google.com
     * 2. Crie um projeto
     * 3. Ative a API "Google+ API" ou "Google Identity"
     * 4. Vá em "Credentials" → "Create Credentials" → "OAuth 2.0 Client ID"
     * 5. Configure tipo "Web application"
     * 6. Copie o Client ID gerado
     * <p>
     * IMPORTANTE - Segurança:
     * - Em PRODUÇÃO: SEMPRE usar variável de ambiente
     * - NUNCA commitar client ID real no código
     * <p>
     * Formato:
     * - Exemplo: "123456789012-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com"
     */
    private final String clientId;

    /**
     * Construtor para injeção via Spring Boot.
     * <p>
     * O Spring Boot mapeia automaticamente:
     * - google.oauth.client-id → clientId
     *
     * @param clientId Google OAuth Client ID
     */
    public GoogleOAuthProperties(String clientId) {
        this.clientId = clientId;
    }

}
