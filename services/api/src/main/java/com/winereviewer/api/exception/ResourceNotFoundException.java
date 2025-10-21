package com.winereviewer.api.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Exceção lançada quando um recurso não é encontrado no banco de dados.
 * <p>
 * Exemplos de uso:
 * <ul>
 *   <li>Review não encontrado ao buscar por ID</li>
 *   <li>Wine não encontrado ao criar review</li>
 *   <li>User não encontrado ao validar ownership</li>
 *   <li>Comment não encontrado ao deletar</li>
 * </ul>
 * <p>
 * Esta exceção resulta em resposta HTTP <strong>404 NOT FOUND</strong>.
 *
 * @author lucas
 * @date 21/10/2025
 */
public class ResourceNotFoundException extends DomainException {

    /**
     * Construtor com mensagem customizada.
     *
     * @param message descrição do recurso não encontrado
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Construtor de conveniência para recursos identificados por UUID.
     * <p>
     * Gera mensagem padronizada: "Review não encontrado. ID: 123e4567-..."
     *
     * @param resourceType nome do recurso (Review, Wine, User, Comment)
     * @param id           UUID do recurso não encontrado
     */
    public ResourceNotFoundException(String resourceType, UUID id) {
        super(String.format("%s não encontrado. ID: %s", resourceType, id));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

}
