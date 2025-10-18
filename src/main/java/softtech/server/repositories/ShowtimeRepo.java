package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import softtech.server.models.Showtime;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepo extends JpaRepository<Showtime, String> {
    @Query("SELECT DISTINCT s.language FROM Showtime s WHERE s.movie.movieId = :movieId")
    List<String> findDistinctLanguagesByMovieId(@Param("movieId") String movieId);
    List<Showtime> findByMovie_MovieId(String movieId);
    List<Showtime> findByRoom_RoomId(String roomId);
    Showtime findByShowtimeId(String showtimeId);
}
