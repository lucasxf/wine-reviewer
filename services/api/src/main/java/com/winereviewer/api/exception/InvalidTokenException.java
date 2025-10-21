package com.winereviewer.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when Google OAuth token validation fails.
 * <p>
 * Casos de uso:
 * - Token expirado
 * - Assinatura inválida
 * - Audience (client ID) incorreto
 * - Token malformado
 * <p>
 * HTTP Status: 401 UNAUTHORIZED
 *
 * @author lucas
 * @date 21/10/2025
 */
public class InvalidTokenException extends DomainException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

}
