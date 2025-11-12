package com.winereviewer.api.service;

import com.winereviewer.api.config.GoogleOAuthProperties;
import com.winereviewer.api.exception.InvalidTokenException;
import com.winereviewer.api.service.impl.GoogleTokenValidatorImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes para GoogleTokenValidator.
 * <p>
 * Nota sobre limitações de teste:
 * O GoogleTokenValidatorImpl usa GoogleIdTokenVerifier internamente,
 * que faz verificações reais com as chaves públicas do Google.
 * <p>
 * Para testar com tokens reais, seria necessário:
 * - Usar Google OAuth Playground para gerar tokens válidos
 * - Configurar testes de integração com Testcontainers
 * <p>
 * Estes testes cobrem:
 * - Inicialização correta do validator
 * - Validação de erros para tokens inválidos/null/vazios
 * - O fluxo completo é testado via AuthServiceTest (com mock do validator)
 *
 * @author lucas
 * @date 21/10/2025
 */
class GoogleTokenValidatorTest {

    /**
     * Teste de inicialização.
     * <p>
     * Verifica que o GoogleTokenValidatorImpl é inicializado corretamente
     * com as propriedades fornecidas.
     */
    @Test
    void shouldCreateValidatorWhenGoogleOAuthPropertiesProvided() {
        // Given
        final var properties = new GoogleOAuthProperties("test-client-id.apps.googleusercontent.com");

        // When
        final var validator = new GoogleTokenValidatorImpl(properties);

        // Then
        assertThat(validator).isNotNull();
    }

    /**
     * Teste de validação de token com formato inválido.
     * <p>
     * Tokens Google ID têm formato específico (JWT com 3 partes separadas por ponto).
     * Este teste verifica que tokens com formato inválido lançam InvalidTokenException.
     */
    @Test
    void shouldThrowInvalidTokenExceptionWhenTokenFormatIsInvalid() {
        // Given
        final var properties = new GoogleOAuthProperties("test-client-id.apps.googleusercontent.com");
        final var validator = new GoogleTokenValidatorImpl(properties);
        final var invalidToken = "invalid-token-format";

        // When/Then
        assertThatThrownBy(() -> validator.validateToken(invalidToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Falha ao validar token do Google");
    }

    /**
     * Teste de validação de token vazio.
     * <p>
     * Tokens vazios devem ser rejeitados imediatamente.
     */
    @Test
    void shouldThrowInvalidTokenExceptionWhenTokenIsEmpty() {
        // Given
        final var properties = new GoogleOAuthProperties("test-client-id.apps.googleusercontent.com");
        final var validator = new GoogleTokenValidatorImpl(properties);

        // When/Then
        assertThatThrownBy(() -> validator.validateToken(""))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Falha ao validar token do Google");
    }

    /**
     * Teste de validação de token null.
     * <p>
     * Tokens null devem ser rejeitados e lançar InvalidTokenException.
     */
    @Test
    void shouldThrowInvalidTokenExceptionWhenTokenIsNull() {
        // Given
        final var properties = new GoogleOAuthProperties("test-client-id.apps.googleusercontent.com");
        final var validator = new GoogleTokenValidatorImpl(properties);

        // When/Then
        assertThatThrownBy(() -> validator.validateToken(null))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Falha ao validar token do Google");
    }

    /**
     * Teste de validação de token com formato JWT válido mas assinatura inválida.
     * <p>
     * Um JWT tem formato "header.payload.signature" (3 partes Base64 separadas por ponto).
     * Este token tem formato correto mas assinatura inválida, então o Google rejeitará.
     */
    @Test
    void shouldThrowInvalidTokenExceptionWhenJWTSignatureIsInvalid() {
        // Given
        final var properties = new GoogleOAuthProperties("test-client-id.apps.googleusercontent.com");
        final var validator = new GoogleTokenValidatorImpl(properties);
        final var fakeJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";

        // When/Then
        assertThatThrownBy(() -> validator.validateToken(fakeJwt))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Falha ao validar token do Google");
    }

}
