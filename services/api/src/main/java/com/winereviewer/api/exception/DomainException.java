package com.winereviewer.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção base para todas as exceções de domínio da aplicação.
 * <p>
 * Esta classe abstrata define o contrato para exceções específicas do domínio,
 * fornecendo informações sobre o status HTTP apropriado para cada tipo de erro.
 * <p>
 * <strong>Por que usar exceções de domínio:</strong>
 * <ul>
 *   <li>Semântica clara: InvalidRatingException é mais descritivo que IllegalArgumentException</li>
 *   <li>Controle de HTTP Status: cada exceção define seu próprio status code</li>
 *   <li>Rastreabilidade: fácil identificar origem do erro no stacktrace</li>
 *   <li>Testabilidade: permite assertions específicas nos testes</li>
 * </ul>
 * <p>
 * <strong>Hierarquia de exceções:</strong>
 * <pre>
 * DomainException (abstrata)
 *   ├── ResourceNotFoundException (404)
 *   ├── InvalidRatingException (400)
 *   ├── UnauthorizedAccessException (403)
 *   └── BusinessRuleViolationException (422)
 * </pre>
 *
 * @author lucas
 * @date 21/10/2025
 */
public abstract class DomainException extends RuntimeException {

    /**
     * Construtor com mensagem de erro.
     *
     * @param message descrição do erro (pode ser em português)
     */
    protected DomainException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e causa raiz.
     *
     * @param message descrição do erro
     * @param cause   exceção original que causou este erro
     */
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Retorna o status HTTP apropriado para esta exceção.
     * <p>
     * Cada subclasse deve implementar este método para definir o código HTTP correto.
     * Exemplos:
     * - ResourceNotFoundException → 404 NOT_FOUND
     * - InvalidRatingException → 400 BAD_REQUEST
     * - UnauthorizedAccessException → 403 FORBIDDEN
     *
     * @return HttpStatus correspondente ao tipo de erro
     */
    public abstract HttpStatus getHttpStatus();

}
