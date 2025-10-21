package com.winereviewer.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando uma regra de negócio é violada.
 * <p>
 * <strong>Diferença entre 400 e 422:</strong>
 * <ul>
 *   <li><strong>400 Bad Request:</strong> sintaxe inválida (JSON malformado, tipos errados)</li>
 *   <li><strong>422 Unprocessable Entity:</strong> sintaxe OK mas viola regras de negócio</li>
 * </ul>
 * <p>
 * <strong>Exemplos de uso:</strong>
 * <ul>
 *   <li>Tentar criar review duplicado para mesmo vinho → 422</li>
 *   <li>Tentar deletar review que tem comentários → 422</li>
 *   <li>Upload de imagem maior que limite → 422</li>
 *   <li>Vinho com ano futuro → 422</li>
 * </ul>
 * <p>
 * <strong>Quando NÃO usar:</strong>
 * - Validação de campos (use Bean Validation com @Valid)
 * - Recurso não encontrado (use ResourceNotFoundException)
 * - Problemas de ownership (use UnauthorizedAccessException)
 * <p>
 * Esta exceção resulta em resposta HTTP <strong>422 UNPROCESSABLE ENTITY</strong>.
 *
 * @author lucas
 * @date 21/10/2025
 */
public class BusinessRuleViolationException extends DomainException {

    /**
     * Construtor com mensagem customizada.
     *
     * @param message descrição da regra violada
     */
    public BusinessRuleViolationException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e causa raiz.
     * <p>
     * Útil quando a violação é detectada por constraint do banco (unique, check, etc).
     *
     * @param message descrição da regra violada
     * @param cause   exceção original (ex: DataIntegrityViolationException)
     */
    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }

}
