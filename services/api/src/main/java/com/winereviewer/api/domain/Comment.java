package com.winereviewer.api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade que representa um comentário em uma avaliação de vinho.
 *
 * @author Lucas Xavier Ferreira
 * @date 18/10/2025
 */
@Getter
@Setter
@Entity
@Table(name = "comment")
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    @PrePersist
    @PreUpdate
    protected void validate() {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Conteúdo do comentário é obrigatório");
        }
        if (review == null) {
            throw new IllegalArgumentException("Review é obrigatório");
        }
        if (author == null) {
            throw new IllegalArgumentException("Autor é obrigatório");
        }
    }

    @PrePersist
    @PreUpdate
    protected void normalize() {
        if (content != null) {
            content = content.trim();
        }
    }

}
