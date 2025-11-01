package com.winereviewer.api.service;

import com.winereviewer.api.application.dto.request.CreateCommentRequest;
import com.winereviewer.api.application.dto.request.UpdateCommentRequest;
import com.winereviewer.api.application.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Interface de serviço para gerenciamento de comentários em avaliações de vinhos.
 * <p>
 * Define operações de negócio para criar, atualizar, buscar e excluir comentários.
 * Implementações devem garantir validações de ownership (autorização) e regras de negócio.
 *
 * @author lucas
 * @date 30/10/2025
 * @since 0.1.0
 */
public interface CommentService {

    /**
     * Adiciona um novo comentário a uma avaliação de vinho.
     * <p>
     * Valida se a avaliação e o usuário existem antes de criar o comentário.
     *
     * @param request dados do comentário (reviewId, text)
     * @param userId ID do usuário autenticado que está criando o comentário
     * @return comentário criado com informações do autor
     * @throws com.winereviewer.api.exception.ResourceNotFoundException se avaliação ou usuário não existir
     */
    CommentResponse addComment(CreateCommentRequest request, UUID userId);

    /**
     * Atualiza o texto de um comentário existente.
     * <p>
     * Valida ownership: apenas o autor do comentário pode atualizá-lo.
     *
     * @param request dados atualizados (commentId, text)
     * @param userId ID do usuário autenticado que está atualizando o comentário
     * @return comentário atualizado
     * @throws com.winereviewer.api.exception.ResourceNotFoundException se comentário ou usuário não existir
     * @throws com.winereviewer.api.exception.UnauthorizedAccessException se userId não for o autor do comentário
     */
    CommentResponse updateComment(UpdateCommentRequest request, UUID userId);

    /**
     * Busca todos os comentários criados por um usuário específico.
     * <p>
     * Retorna comentários ordenados por data de criação (mais recentes primeiro).
     *
     * @param userId ID do usuário
     * @param pagination parâmetros de paginação (page, size, sort)
     * @return página de comentários do usuário
     * @throws com.winereviewer.api.exception.ResourceNotFoundException se usuário não existir
     */
    Page<CommentResponse> getCommentsPerUser(UUID userId, Pageable pagination);

    /**
     * Busca todos os comentários de uma avaliação específica.
     * <p>
     * Retorna comentários ordenados por data de criação (mais antigos primeiro).
     *
     * @param reviewId ID da avaliação
     * @param pagination parâmetros de paginação (page, size, sort)
     * @return página de comentários da avaliação
     * @throws com.winereviewer.api.exception.ResourceNotFoundException se avaliação não existir
     */
    Page<CommentResponse> getCommentsPerReview(UUID reviewId, Pageable pagination);

    /**
     * Exclui um comentário existente.
     * <p>
     * Valida ownership: apenas o autor do comentário pode excluí-lo.
     *
     * @param commentId ID do comentário a ser excluído
     * @param userId ID do usuário autenticado que está excluindo o comentário
     * @throws com.winereviewer.api.exception.ResourceNotFoundException se comentário não existir
     * @throws com.winereviewer.api.exception.UnauthorizedAccessException se userId não for o autor do comentário
     */
    void deleteComment(UUID commentId, UUID userId);

}
