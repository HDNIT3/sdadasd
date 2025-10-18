package softtech.server.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import softtech.server.models.Review;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ReviewRepo extends JpaRepository<Review, String> {

    @Query("SELECT r FROM reviews r " +
            "WHERE (:movieTitle IS NULL OR r.movie.title LIKE %:movieTitle%) " +
            "AND (:from IS NULL OR r.createdAt >= :from) " +
            "AND (:to IS NULL OR r.createdAt < :to) " +
            "ORDER BY r.createdAt DESC")
    List<Review> findFilteredReviews(
            @Param("movieTitle") String movieTitle,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("SELECT AVG(r.rating) FROM reviews r WHERE r.movie.movieId = :movieId")
    Double calculateAverageRatingByMovieId(@Param("movieId") String movieId);

    @Query("SELECT COUNT(r) FROM reviews r WHERE r.movie.movieId = :movieId")
    Long countByMovieId(@Param("movieId") String movieId);

    @Query("SELECT r FROM reviews r WHERE r.movie.movieId = :movieId ORDER BY r.createdAt DESC")
    List<Review> findByMovieId(@Param("movieId") String movieId);

    @Query("SELECT r FROM reviews r WHERE r.customer.customerId = :customerId ORDER BY r.createdAt DESC")
    List<Review> findByCustomerId(@Param("customerId") String customerId);

    List<Review> findByCustomer_CustomerId(String customerId);

}