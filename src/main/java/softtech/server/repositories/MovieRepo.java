package softtech.server.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import softtech.server.enums.AgeRating;
import softtech.server.enums.Genre;
import softtech.server.models.Movie;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface MovieRepo extends JpaRepository<Movie, String>, JpaSpecificationExecutor<Movie> {
    Page<Movie> findAll(Pageable pageable);
    List<Movie> findByRatingGreaterThanEqual(double rating);
    List<Movie> findByReleaseDateAfter(LocalDate today);
    List<Movie> findByMovieIdIn(List<String> ids);
    
    // Lấy phim theo thể loại
    @Query("select m from Movie m join m.genres g where g = :genre")
    List<Movie> findByGenresJoin(@Param("genre") Genre genre);

    // Lấy phim theo diễn viên
    @Query("select m from Movie m join m.actors a where a = :actor")
    List<Movie> findByActorsJoin(@Param("actor") String actor);
}
