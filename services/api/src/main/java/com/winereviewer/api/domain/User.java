package com.winereviewer.api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * @author lucas
 * @date 18/10/2025 14:05
 */
@Getter
@Setter
@Entity
@Table(name = "app_user")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "email", nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "google_id", unique = true, nullable = true)
    private String googleId;

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
        if (displayName != null) {
            displayName = displayName.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        if (avatarUrl != null) {
            avatarUrl = avatarUrl.trim();
        }

        // Valida integridade da entidade (não são regras de negócio, são pré-condições)
        // IllegalArgumentException é apropriado aqui pois indica erro de programação
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Nome de exibição é obrigatório");
        }
    }

}
