package com.winereviewer.api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade que representa um vinho no catálogo.
 *
 * @author Lucas Xavier Ferreira
 * @date 18/10/2025
 */
@Getter
@Setter
@Entity
@Table(name = "wine")
@NoArgsConstructor
@AllArgsConstructor
public class Wine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "winery", length = 160)
    private String winery;

    @Column(name = "country", length = 80)
    private String country;

    @Column(name = "grape", length = 80)
    private String grape;

    @Column(name = "year")
    private Integer year;

    @Column(name = "image_url")
    private String imageUrl;

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
        if (year != null && (year < 1900 || year > 2100)) {
            throw new IllegalArgumentException("Ano deve estar entre 1900 e 2100");
        }
    }

    @PrePersist
    @PreUpdate
    protected void normalize() {
        if (name != null) {
            name = name.trim();
        }
        if (winery != null) {
            winery = winery.trim();
        }
        if (country != null) {
            country = country.trim();
        }
        if (grape != null) {
            grape = grape.trim();
        }
        if (imageUrl != null) {
            imageUrl = imageUrl.trim();
        }
    }

}
