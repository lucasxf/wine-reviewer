package com.winereviewer.api.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes para exceções de domínio.
 * <p>
 * Valida:
 * - Mensagens de erro corretas
 * - HTTP status codes apropriados
 * - Construtores de conveniência
 * - Hierarquia de herança
 *
 * @author lucas
 * @date 21/10/2025
 */
@DisplayName("Domain Exceptions Tests")
class DomainExceptionTest {

    // ==================== ResourceNotFoundException ====================

    @Test
    @DisplayName("ResourceNotFoundException: deve criar com mensagem customizada")
    void resourceNotFound_withCustomMessage_shouldCreateCorrectly() {
        // Arrange
        final String message = "Vinho não encontrado";

        // Act
        final var exception = new ResourceNotFoundException(message);

        // Assert
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception).isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("ResourceNotFoundException: deve criar com resourceType e UUID")
    void resourceNotFound_withTypeAndId_shouldGenerateStandardMessage() {
        // Arrange
        final String resourceType = "Review";
        final UUID id = UUID.randomUUID();

        // Act
        final var exception = new ResourceNotFoundException(resourceType, id);

        // Assert
        assertThat(exception.getMessage())
                .contains(resourceType)
                .contains(id.toString())
                .contains("não encontrado");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== InvalidRatingException ====================

    @Test
    @DisplayName("InvalidRatingException: deve criar com mensagem customizada")
    void invalidRating_withCustomMessage_shouldCreateCorrectly() {
        // Arrange
        final String message = "Rating deve estar entre 1 e 5";

        // Act
        final var exception = new InvalidRatingException(message);

        // Assert
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception).isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("InvalidRatingException: deve gerar mensagem para rating inválido (0)")
    void invalidRating_withZero_shouldGenerateStandardMessage() {
        // Arrange
        final int invalidRating = 0;

        // Act
        final var exception = new InvalidRatingException(invalidRating);

        // Assert
        assertThat(exception.getMessage())
                .contains("Rating inválido: 0")
                .contains("Permitido: 1-5");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("InvalidRatingException: deve gerar mensagem para rating inválido (6)")
    void invalidRating_withSix_shouldGenerateStandardMessage() {
        // Arrange
        final int invalidRating = 6;

        // Act
        final var exception = new InvalidRatingException(invalidRating);

        // Assert
        assertThat(exception.getMessage())
                .contains("Rating inválido: 6")
                .contains("Permitido: 1-5");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==================== UnauthorizedAccessException ====================

    @Test
    @DisplayName("UnauthorizedAccessException: deve criar com mensagem customizada")
    void unauthorizedAccess_withCustomMessage_shouldCreateCorrectly() {
        // Arrange
        final String message = "Você não tem permissão para deletar este recurso";

        // Act
        final var exception = new UnauthorizedAccessException(message);

        // Assert
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exception).isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("UnauthorizedAccessException: deve criar com userId e resourceType")
    void unauthorizedAccess_withUserIdAndType_shouldGenerateStandardMessage() {
        // Arrange
        final UUID userId = UUID.randomUUID();
        final String resourceType = "Comment";

        // Act
        final var exception = new UnauthorizedAccessException(userId, resourceType);

        // Assert
        assertThat(exception.getMessage())
                .contains(userId.toString())
                .contains(resourceType)
                .contains("não tem permissão");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ==================== BusinessRuleViolationException ====================

    @Test
    @DisplayName("BusinessRuleViolationException: deve criar com mensagem customizada")
    void businessRuleViolation_withCustomMessage_shouldCreateCorrectly() {
        // Arrange
        final String message = "Não é possível deletar review com comentários";

        // Act
        final var exception = new BusinessRuleViolationException(message);

        // Assert
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(exception).isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("BusinessRuleViolationException: deve criar com mensagem e causa")
    void businessRuleViolation_withMessageAndCause_shouldCreateCorrectly() {
        // Arrange
        final String message = "Violação de constraint no banco";
        final Throwable cause = new RuntimeException("Duplicate key");

        // Act
        final var exception = new BusinessRuleViolationException(message, cause);

        // Assert
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ==================== Hierarquia de Exceções ====================

    @Test
    @DisplayName("Todas as exceções devem herdar de DomainException")
    void allExceptions_shouldExtendDomainException() {
        // Assert
        assertThat(new ResourceNotFoundException("test")).isInstanceOf(DomainException.class);
        assertThat(new InvalidRatingException("test")).isInstanceOf(DomainException.class);
        assertThat(new UnauthorizedAccessException("test")).isInstanceOf(DomainException.class);
        assertThat(new BusinessRuleViolationException("test")).isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Todas as exceções devem herdar de RuntimeException")
    void allExceptions_shouldExtendRuntimeException() {
        // Assert
        assertThat(new ResourceNotFoundException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new InvalidRatingException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new UnauthorizedAccessException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new BusinessRuleViolationException("test")).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Exceções devem ser lançáveis e capturáveis")
    void exceptions_shouldBeThrowableAndCatchable() {
        // Arrange
        final UUID reviewId = UUID.randomUUID();

        // Assert - ResourceNotFoundException
        assertThatThrownBy(() -> {
            throw new ResourceNotFoundException("Review", reviewId);
        })
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Review")
                .hasMessageContaining(reviewId.toString());

        // Assert - InvalidRatingException
        assertThatThrownBy(() -> {
            throw new InvalidRatingException(10);
        })
                .isInstanceOf(InvalidRatingException.class)
                .hasMessageContaining("Rating inválido: 10");

        // Assert - UnauthorizedAccessException
        assertThatThrownBy(() -> {
            throw new UnauthorizedAccessException(UUID.randomUUID(), "Review");
        })
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("não tem permissão");

        // Assert - BusinessRuleViolationException
        assertThatThrownBy(() -> {
            throw new BusinessRuleViolationException("Regra violada");
        })
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Regra violada");
    }
}
