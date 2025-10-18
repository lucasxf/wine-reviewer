package com.winereviewer.api.repository;

import com.winereviewer.api.domain.Wine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * @author lucas
 * @date 18/10/2025 14:30
 */
public interface WineRepository extends JpaRepository<Wine, UUID> {

    List<Wine> findByNameContainingIgnoreCase(String name);

    List<Wine> findByCountry(String country);

    List<Wine> findByWinery(String winery);

    List<Wine> findByGrapeContainingIgnoreCase(String grape);

}
