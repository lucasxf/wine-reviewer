package com.winereviewer.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando uma avaliação (rating) está fora do range válido.
 * <p>
 * <strong>Regra de negócio:</strong> Rating deve estar entre 1 e 5 (glasses, não stars!).
 * <p>
 * <strong>Exemplos que lançam esta exceção:</strong>
 * <ul>
 *   <li>Rating = 0 → inválido (mínimo é 1)</li>
 *   <li>Rating = 6 → inválido (máximo é 5)</li>
 *   <li>Rating = -1 → inválido</li>
 *   <li>Rating = null → inválido (tratado por Bean Validation antes)</li>
 * </ul>
 * <p>
 * Esta exceção resulta em resposta HTTP <strong>400 BAD REQUEST</strong>.
 *
 * @author lucas
 * @date 21/10/2025
 */
public class InvalidRatingException extends DomainException {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    /**
     * Construtor com mensagem customizada.
     *
     * @param message descrição do erro de validação
     */
    public InvalidRatingException(String message) {
        super(message);
    }

    /**
     * Construtor de conveniência que gera mensagem padronizada.
     * <p>
     * Mensagem gerada: "Rating inválido: 6. Permitido: 1-5"
     *
     * @param actualRating valor inválido recebido
     */
    public InvalidRatingException(int actualRating) {
        super(String.format("Rating inválido: %d. Permitido: %d-%d", actualRating, MIN_RATING, MAX_RATING));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
