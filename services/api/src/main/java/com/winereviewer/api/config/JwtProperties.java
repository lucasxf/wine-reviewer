package com.winereviewer.api.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração para JWT.
 * <p>
 * Carrega configurações do application.yml com prefix "jwt".
 * <p>
 * Exemplo de application.yml:
 * <pre>
 * jwt:
 *   secret: your-secret-key-must-be-at-least-256-bits
 *   expiration: 3600000  # 1 hora em milissegundos
 * </pre>
 *
 * @author lucas
 * @date 20/10/2025
 */
@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Chave secreta para assinar tokens JWT.
     * <p>
     * REQUISITOS:
     * - Mínimo 256 bits (32 caracteres)
     * - Usar caracteres aleatórios (letras, números, símbolos)
     * - Em PRODUÇÃO: SEMPRE usar variável de ambiente (NUNCA hardcoded)
     * <p>
     * Exemplo seguro:
     * - export JWT_SECRET="a8f5f167f44f4964e6c998dee827110c3af5b7e4d9e9e5f8b5f5f8e5d9e5f8e5"
     * - application-prod.yml: secret: ${JWT_SECRET}
     */
    private final String secret;

    /**
     * Tempo de expiração do token em milissegundos.
     * <p>
     * Recomendações:
     * - Development: 3_600_000 (1 hora)
     * - Production: 900_000 (15 minutos) + refresh token
     * <p>
     * Conversões úteis:
     * - 15 min = 900_000 ms
     * - 30 min = 1_800_000 ms
     * - 1 hora = 3_600_000 ms
     * - 24 horas = 86_400_000 ms
     */
    private final Long expiration;

    /**
     * Construtor para injeção via Spring Boot.
     * <p>
     * O Spring Boot mapeia automaticamente:
     * - jwt.secret → secret
     * - jwt.expiration → expiration
     *
     * @param secret     chave secreta (mínimo 32 caracteres)
     * @param expiration tempo de expiração em milissegundos
     */
    public JwtProperties(String secret, Long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

}
