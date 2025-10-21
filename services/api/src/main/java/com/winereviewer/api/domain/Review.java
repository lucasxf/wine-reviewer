package com.winereviewer.api.domain;

import com.winereviewer.api.exception.InvalidRatingException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade que representa uma avaliação de vinho feita por um usuário.
 *
 * @author Lucas Xavier Ferreira
 * @date 18/10/2025
 */
@Getter
@Setter
@Entity
@Table(name = "review")
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wine_id", nullable = false)
    private Wine wine;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        validateAndNormalize();
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        validateAndNormalize();
        updatedAt = Instant.now();
    }

    private void validateAndNormalize() {
        // Normaliza
        if (notes != null) {
            notes = notes.trim();
        }
        if (imageUrl != null) {
            imageUrl = imageUrl.trim();
        }

        // Valida rating usando exceção de domínio
        if (rating == null || rating < 1 || rating > 5) {
            throw new InvalidRatingException(rating != null ? rating : 0);
        }

        // Valida obrigatoriedade de user e wine
        // Nota: Estas validações são de integridade da entidade, não de domínio de negócio
        // IllegalArgumentException é apropriado aqui pois indica erro de programação
        if (user == null) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }
        if (wine == null) {
            throw new IllegalArgumentException("Vinho é obrigatório");
        }
    }

}
