package com.winereviewer.api.controller;

import com.winereviewer.api.application.dto.request.CreateReviewRequest;
import com.winereviewer.api.application.dto.request.UpdateReviewRequest;
import com.winereviewer.api.application.dto.response.ReviewResponse;
import com.winereviewer.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller para gerenciamento de avaliações de vinhos.
 * <p>
 * Fornece endpoints REST para criar, atualizar, buscar e deletar reviews.
 *
 * @author lucas
 * @date 19/10/2025 09:13
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService service;

    /**
     * Cria uma nova avaliação de vinho.
     *
     * @param request dados da avaliação (wineId, rating, notes, imageUrl)
     * @return avaliação criada com status 201 Created
     */
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
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
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
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable UUID reviewId) {
        log.info("Recebida requisição para buscar review: {}", reviewId);
        final var review = service.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    /**
     * Lista avaliações com filtros opcionais.
     *
     * @param wineId filtro por vinho (opcional)
     * @param userId filtro por usuário (opcional)
     * @return lista de avaliações com status 200 OK
     */
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> listReviews(
            @RequestParam(required = false) UUID wineId,
            @RequestParam(required = false) UUID userId) {
        log.info("Recebida requisição para listar reviews. Filtros - wineId: {}, userId: {}", wineId, userId);
        // TODO: implementar no service
        throw new UnsupportedOperationException("Endpoint não implementado");
    }

    /**
     * Deleta uma avaliação.
     * <p>
     * Apenas o autor da review pode deletá-la (validação de ownership).
     *
     * @param reviewId ID da review a deletar
     * @return status 204 No Content se deletada com sucesso, ou 404/403
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        log.info("Recebida requisição para deletar review: {}", reviewId);
        // TODO capture authenticated user ID via JWT
        // TODO: implementar no service (validar ownership!)
        throw new UnsupportedOperationException("Endpoint não implementado");
    }

}
