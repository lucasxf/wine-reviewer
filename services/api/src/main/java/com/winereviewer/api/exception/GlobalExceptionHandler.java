package com.winereviewer.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler para toda a aplicação.
 * <p>
 * Captura exceptions e converte em respostas JSON padronizadas.
 * <p>
 * <strong>Hierarquia de tratamento:</strong>
 * <ol>
 *   <li>Exceções de domínio específicas (ResourceNotFoundException, InvalidRatingException, etc.)</li>
 *   <li>Exceções de validação (MethodArgumentNotValidException)</li>
 *   <li>Exceções de runtime legadas (IllegalArgumentException, SecurityException) - deprecadas</li>
 *   <li>Exceção genérica (fallback para erros inesperados)</li>
 * </ol>
 *
 * @author lucas
 * @date 19/10/2025
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata todas as exceções de domínio (DomainException e subclasses).
     * <p>
     * Este handler captura:
     * - ResourceNotFoundException → 404
     * - InvalidRatingException → 400
     * - UnauthorizedAccessException → 403
     * - BusinessRuleViolationException → 422
     * <p>
     * O status HTTP é determinado pelo método getHttpStatus() de cada exceção.
     *
     * @param ex exceção de domínio
     * @return erro padronizado com status específico da exceção
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        final HttpStatus status = ex.getHttpStatus();

        log.warn("Exceção de domínio [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());

        var errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage()
        );

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Trata erros de validação de Bean Validation (@Valid).
     *
     * @param ex exception de validação
     * @return mapa de erros por campo com status 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Erro de validação: {}", errors);

        var errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                errors.toString()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Trata IllegalArgumentException (entidade não encontrada, parâmetros inválidos).
     * <p>
     * <strong>DEPRECADO:</strong> Use {@link ResourceNotFoundException} ao invés desta exceção.
     * Este handler existe apenas para manter compatibilidade com código legado.
     *
     * @param ex exception de argumento inválido
     * @return erro padronizado com status 404 Not Found
     * @deprecated Use ResourceNotFoundException para recursos não encontrados
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("[DEPRECADO] IllegalArgumentException: {}. Use ResourceNotFoundException.", ex.getMessage());

        var errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Trata SecurityException (validação de ownership, permissões).
     * <p>
     * <strong>DEPRECADO:</strong> Use {@link UnauthorizedAccessException} ao invés desta exceção.
     * Este handler existe apenas para manter compatibilidade com código legado.
     *
     * @param ex exception de segurança
     * @return erro padronizado com status 403 Forbidden
     * @deprecated Use UnauthorizedAccessException para violações de acesso
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex) {
        log.warn("[DEPRECADO] SecurityException: {}. Use UnauthorizedAccessException.", ex.getMessage());

        var errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Trata UnsupportedOperationException (endpoints não implementados).
     *
     * @param ex exception de operação não suportada
     * @return erro padronizado com status 501 Not Implemented
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedOperationException(UnsupportedOperationException ex) {
        log.info("Endpoint não implementado acessado: {}", ex.getMessage());

        var errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_IMPLEMENTED.value(),
                "Funcionalidade não implementada",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(errorResponse);
    }

    /**
     * Trata qualquer exceção não mapeada (fallback).
     *
     * @param ex exception genérica
     * @return erro padronizado com status 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erro inesperado: ", ex);

        var errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor",
                "Ocorreu um erro inesperado. Por favor, tente novamente."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * DTO para resposta de erro padronizada.
     */
    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message
    ) {
    }

}
