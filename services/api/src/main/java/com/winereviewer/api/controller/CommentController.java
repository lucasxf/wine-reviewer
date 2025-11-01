package com.winereviewer.api.controller;

import com.winereviewer.api.application.dto.request.CreateCommentRequest;
import com.winereviewer.api.application.dto.request.UpdateCommentRequest;
import com.winereviewer.api.application.dto.response.CommentResponse;
import com.winereviewer.api.service.CommentService;
import com.winereviewer.api.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller para gerenciamento de comentários em avaliações de vinhos.
 * <p>
 * Fornece endpoints REST para criar, atualizar, buscar e excluir comentários.
 * Todos os endpoints requerem autenticação JWT.
 *
 * @author lucas
 * @date 31/10/2025
 * @since 0.1.0
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/comments")
@Tag(name = "Comments", description = "API de gerenciamento de comentários em avaliações de vinhos")
public class CommentController {

    private final CommentService commentService;
    private final ReviewService reviewService;

    /**
     * Cria um novo comentário em uma avaliação de vinho.
     *
     * @param request dados do comentário (reviewId, text)
     * @param authentication contexto de autenticação Spring Security
     * @return comentário criado com status 201 Created
     */
    @Operation(
            summary = "Criar comentário em avaliação",
            description = "Adiciona um novo comentário a uma avaliação de vinho específica. Requer autenticação (JWT)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Comentário criado com sucesso",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos (reviewId nulo, texto vazio ou em branco)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token de autenticação inválido ou expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - autenticação requerida"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avaliação ou usuário não encontrado"
            )
    })
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @RequestBody @Valid CreateCommentRequest request,
            Authentication authentication) {

        final var userId = UUID.fromString(authentication.getName());
        log.info("Received request to create comment for user {}", userId);

        final var response = commentService.addComment(request, userId);
        log.info("Successfully created comment {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Atualiza um comentário existente.
     *
     * @param request dados atualizados (commentId, text)
     * @param authentication contexto de autenticação Spring Security
     * @return comentário atualizado com status 200 OK
     */
    @Operation(
            summary = "Atualizar comentário",
            description = "Atualiza o texto de um comentário existente. Apenas o autor do comentário pode atualizá-lo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comentário atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos (commentId nulo, texto vazio ou em branco)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token de autenticação inválido ou expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário não é o autor do comentário"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comentário ou usuário não encontrado"
            )
    })
    @PutMapping
    public ResponseEntity<CommentResponse> updateComment(
            @RequestBody @Valid UpdateCommentRequest request,
            Authentication authentication) {
        final var userId = UUID.fromString(authentication.getName());
        log.info("Received request to update comment for user {}", userId);

        final var response = commentService.updateComment(request, userId);
        log.info("Successfully updated comment {}", response.id());
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos os comentários criados pelo usuário autenticado.
     *
     * @param authentication contexto de autenticação Spring Security
     * @param pageable parâmetros de paginação (page, size, sort)
     * @return página de comentários do usuário com status 200 OK
     */
    @Operation(
            summary = "Listar comentários do usuário autenticado",
            description = """
                    Lista todos os comentários criados pelo usuário autenticado, ordenados por data de criação (mais recentes primeiro).

                    Parâmetros de paginação aceitos:
                    - page: número da página (começa em 0, padrão: 0)
                    - size: tamanho da página (padrão: 20)
                    - sort: campo de ordenação (ex: createdAt,desc)

                    Exemplos de uso:
                    - GET /comments?page=0&size=10
                    - GET /comments?page=1&size=5&sort=createdAt,asc
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de comentários retornada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token de autenticação inválido ou expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - autenticação requerida"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado"
            )
    })
    @GetMapping
    public ResponseEntity<Page<CommentResponse>> getCommentsPerUser(
            Authentication authentication,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        final var userId = UUID.fromString(authentication.getName());
        log.info("Received request to get all comments for user {}", userId);

        final Page<CommentResponse> commentsPerUserComments =
                commentService.getCommentsPerUser(userId, pageable);
        log.info("Successfully got all comments for user {}", userId);
        return ResponseEntity.ok(commentsPerUserComments);
    }

    /**
     * Lista todos os comentários de uma avaliação específica.
     *
     * @param reviewId ID da avaliação
     * @param authentication contexto de autenticação Spring Security
     * @param pageable parâmetros de paginação (page, size, sort)
     * @return página de comentários da avaliação com status 200 OK
     */
    @Operation(
            summary = "Listar comentários de uma avaliação",
            description = """
                    Lista todos os comentários de uma avaliação específica, ordenados por data de criação (mais antigos primeiro).

                    Parâmetros de paginação aceitos:
                    - page: número da página (começa em 0, padrão: 0)
                    - size: tamanho da página (padrão: 20)
                    - sort: campo de ordenação (ex: createdAt,asc)

                    Exemplos de uso:
                    - GET /comments/{reviewId}?page=0&size=10
                    - GET /comments/{reviewId}?page=1&size=5&sort=createdAt,desc
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de comentários retornada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token de autenticação inválido ou expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - autenticação requerida"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avaliação não encontrada"
            )
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<Page<CommentResponse>> getCommentsPerReview(
            @Parameter(description = "ID da avaliação", required = true)
            @PathVariable UUID reviewId,
            Authentication authentication,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable) {
        log.info("Received request to get all comments for review {}", reviewId);

        final Page<CommentResponse> commentsPerReview =
                commentService.getCommentsPerReview(reviewId, pageable);
        log.info("Successfully got all comments for review {}", reviewId);
        return ResponseEntity.ok(commentsPerReview);
    }

    /**
     * Exclui um comentário existente.
     *
     * @param commentId ID do comentário a ser excluído
     * @param authentication contexto de autenticação Spring Security
     * @return status 204 No Content se excluído com sucesso
     */
    @Operation(
            summary = "Excluir comentário",
            description = "Remove um comentário existente. Apenas o autor do comentário pode excluí-lo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Comentário excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token de autenticação inválido ou expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário não é o autor do comentário"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comentário não encontrado"
            )
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "ID do comentário a ser excluído", required = true)
            @PathVariable UUID commentId,
            Authentication authentication) {
        final var userId = UUID.fromString(authentication.getName());
        log.info("Received request to delete comment {} by user {}", commentId, userId);

        commentService.deleteComment(commentId, userId);
        log.info("Successfully deleted comment {}", commentId);
        return ResponseEntity.noContent().build();
    }

}
