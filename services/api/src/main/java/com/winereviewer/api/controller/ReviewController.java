package com.winereviewer.api.controller;

import com.winereviewer.api.application.dto.request.CreateReviewRequest;
import com.winereviewer.api.application.dto.request.UpdateReviewRequest;
import com.winereviewer.api.application.dto.response.ReviewResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller para gerenciamento de avaliações de vinhos.
 * <p>
 * Fornece endpoints REST para criar, atualizar, buscar e deletar reviews.
 *
 * @author lucas
 * @date 19/10/2025 09:13
 * @since 0.0.0
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/reviews")
@Tag(name = "Reviews", description = "API de gerenciamento de avaliações de vinhos")
public class ReviewController {

    private final ReviewService service;

    /**
     * Cria uma avaliação de vinho.
     *
     * @param request dados da avaliação (wineId, rating, notes, imageUrl)
     * @return avaliação criada com status 201 Created
     */
    @Operation(
            summary = "Criar avaliação de vinho",
            description = "Cria uma nova avaliação para um vinho específico. Requer autenticação (JWT)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Avaliação criada com sucesso",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos (rating fora do range 1-5, campos obrigatórios faltando)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vinho ou usuário não encontrado"
            )
    })
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @RequestBody @Valid CreateReviewRequest request) {
        log.info("Recebida requisição para criar review do vinho: {}", request.wineId());
        // TODO capture authenticated user ID via JWT
        final var review = service.createReview(request, UUID.randomUUID());
        log.info("Review criada com sucesso. ID: {}", review.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    /**
     * Atualiza uma avaliação existente.
     *
     * @param reviewId ID da review a atualizar
     * @param request  dados atualizados (rating, notes, imageUrl - todos opcionais)
     * @return avaliação atualizada com status 200 OK
     */
    @Operation(
            summary = "Atualizar avaliação",
            description = "Atualiza uma avaliação existente. Apenas o autor pode atualizar sua própria avaliação."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Avaliação atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Rating inválido (fora do range 1-5)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário não é o autor da avaliação"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avaliação não encontrada"
            )
    })
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @Parameter(description = "ID da avaliação", required = true)
            @PathVariable UUID reviewId,
            @RequestBody @Valid UpdateReviewRequest request) {
        log.info("Recebida requisição para atualizar review: {}", reviewId);
        // TODO capture authenticated user ID via JWT
        final var review = service.updateReview(reviewId, request, UUID.randomUUID());
        log.info("Review atualizada com sucesso: {}", reviewId);
        return ResponseEntity.ok(review);
    }

    /**
     * Busca uma avaliação pelo ID.
     *
     * @param reviewId ID da review
     * @return avaliação encontrada com status 200 OK, ou 404 Not Found
     */
    @Operation(
            summary = "Buscar avaliação por ID",
            description = "Retorna os detalhes de uma avaliação específica pelo seu ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Avaliação encontrada",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avaliação não encontrada"
            )
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(
            @Parameter(description = "ID da avaliação", required = true)
            @PathVariable UUID reviewId) {
        log.info("Recebida requisição para buscar review: {}", reviewId);
        final var review = service.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    /**
     * Lista avaliações com filtros opcionais e paginação.
     *
     * @param wineId   filtro por vinho (opcional)
     * @param userId   filtro por usuário (opcional)
     * @param pageable parâmetros de paginação (page, size, sort)
     * @return página de avaliações com status 200 OK
     */
    @Operation(
            summary = "Listar avaliações",
            description = """
                    Lista todas as avaliações com filtros opcionais e paginação.

                    Parâmetros de paginação aceitos:
                    - page: número da página (começa em 0, padrão: 0)
                    - size: tamanho da página (padrão: 20)
                    - sort: campo de ordenação (ex: createdAt,desc ou rating,asc)

                    Exemplos de uso:
                    - GET /reviews?page=0&size=10&sort=createdAt,desc
                    - GET /reviews?wineId=123&page=0&size=5
                    - GET /reviews?userId=456&sort=rating,desc
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de avaliações retornada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vinho ou usuário não encontrado (quando filtros são fornecidos)"
            )
    })
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> listReviews(
            @Parameter(description = "Filtrar por ID do vinho")
            @RequestParam(required = false) UUID wineId,
            @Parameter(description = "Filtrar por ID do usuário")
            @RequestParam(required = false) UUID userId,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("Recebida requisição para listar reviews. Filtros - wineId: {}, userId: {}", wineId, userId);
        final var reviews = service.listReviews(wineId, userId, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Deleta uma avaliação.
     * <p>
     * Apenas o autor da review pode deletá-la (validação de ownership).
     *
     * @param reviewId ID da review a deletar
     * @return status 204 No Content se deletada com sucesso, ou 404/403
     */
    @Operation(
            summary = "Deletar avaliação",
            description = "Deleta uma avaliação existente. Apenas o autor pode deletar sua própria avaliação."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Avaliação deletada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário não é o autor da avaliação"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avaliação não encontrada"
            )
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "ID da avaliação", required = true)
            @PathVariable UUID reviewId) {
        log.info("Recebida requisição para deletar review: {}", reviewId);
        // TODO capture authenticated user ID via JWT
        service.deleteReview(reviewId, UUID.randomUUID());
        log.info("Review deletada com sucesso: {}", reviewId);
        return ResponseEntity.noContent().build();
    }

}
