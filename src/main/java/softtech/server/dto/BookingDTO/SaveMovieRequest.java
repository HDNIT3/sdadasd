package softtech.server.dto.BookingDTO;

import lombok.Data;
import java.util.List;

@Data
public class SaveMovieRequest {
    private String title;
    private String description;
    private Integer duration;
    private String ageRating;
    private String releaseDate;
    private Double rating;
    private List<String> genres;
    private List<String> cast;
}
