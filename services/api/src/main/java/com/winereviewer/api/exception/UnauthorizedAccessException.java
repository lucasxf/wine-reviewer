package com.winereviewer.api.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Exceção lançada quando um usuário tenta acessar/modificar recurso de outro usuário.
 * <p>
 * <strong>Diferença entre 401 e 403:</strong>
 * <ul>
 *   <li><strong>401 Unauthorized:</strong> usuário não autenticado (sem token ou token inválido)</li>
 *   <li><strong>403 Forbidden:</strong> usuário autenticado mas sem permissão (ownership violation)</li>
 * </ul>
 * <p>
 * <strong>Exemplos de uso:</strong>
 * <ul>
 *   <li>User A tenta deletar review do User B → 403</li>
 *   <li>User A tenta editar comentário do User B → 403</li>
 *   <li>Request sem token JWT → 401 (tratado pelo Spring Security)</li>
 * </ul>
 * <p>
 * Esta exceção resulta em resposta HTTP <strong>403 FORBIDDEN</strong>.
 *
 * @author lucas
 * @date 21/10/2025
 */
public class UnauthorizedAccessException extends DomainException {

    /**
     * Construtor com mensagem customizada.
     *
     * @param message descrição da violação de acesso
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    /**
     * Construtor de conveniência para violações de ownership.
     * <p>
     * Mensagem gerada: "Usuário 123e... não tem permissão para modificar este Review"
     *
     * @param userId       UUID do usuário que tentou a ação
     * @param resourceType tipo do recurso (Review, Comment, etc)
     */
    public UnauthorizedAccessException(UUID userId, String resourceType) {
        super(String.format("Usuário %s não tem permissão para modificar este %s", userId, resourceType));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }

}
