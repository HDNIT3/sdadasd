package softtech.server.dto.MovieDTO;

import lombok.Builder;
import lombok.Data;
import softtech.server.dto.ReviewDTO.ReviewDTO;
import softtech.server.dto.BookingDTO.ShowtimeDTO;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MovieDTO {
    private String movieId;
    private String title;
    private String posterUrl;
    private double rating;
    private int duration;
    private List<String> genres;
    private String ageRating;
    private List<String> languages;
    private LocalDate releaseDate;
    private List<String> cast;
    private List<ShowtimeDTO> showtimes;
    private List<ReviewDTO> reviews;
}
