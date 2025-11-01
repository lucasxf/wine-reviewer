package com.winereviewer.api.repository;

import com.winereviewer.api.domain.Comment;
import com.winereviewer.api.domain.Review;
import com.winereviewer.api.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositório Spring Data JPA para acesso a dados de comentários.
 * <p>
 * Fornece operações de persistência para a entidade Comment, incluindo:
 * - CRUD básico (herdado de JpaRepository)
 * - Busca de comentários por avaliação (ordenados cronologicamente)
 * - Busca de comentários por autor (ordenados inversamente)
 * - Contagem de comentários por avaliação
 * <p>
 * Métodos customizados seguem convenção de nomenclatura Spring Data JPA (query derivation).
 *
 * @author lucas
 * @date 18/10/2025
 * @since 0.1.0
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Busca todos os comentários de uma avaliação específica.
     * <p>
     * Comentários são retornados em ordem cronológica (mais antigos primeiro),
     * seguindo padrão de exibição em threads de comentários.
     *
     * @param review entidade da avaliação
     * @param pagination parâmetros de paginação
     * @return página de comentários da avaliação
     */
    Page<Comment> findByReviewOrderByCreatedAtAsc(Review review, Pageable pagination);

    /**
     * Busca todos os comentários criados por um autor específico.
     * <p>
     * Comentários são retornados em ordem cronológica inversa (mais recentes primeiro),
     * seguindo padrão de histórico de atividades do usuário.
     *
     * @param author entidade do usuário autor
     * @param pagination parâmetros de paginação
     * @return página de comentários do autor
     */
    Page<Comment> findByAuthorOrderByCreatedAtDesc(User author, Pageable pagination);

    /**
     * Conta o número total de comentários em uma avaliação específica.
     * <p>
     * Utilizado para exibir contador de comentários nas listagens de avaliações.
     *
     * @param reviewId ID da avaliação
     * @return número total de comentários da avaliação
     */
    @Query("SELECT COUNT(c) FROM Comment c JOIN c.review r WHERE r.id = :reviewId")
    long countCommentByReview(UUID reviewId);

}
