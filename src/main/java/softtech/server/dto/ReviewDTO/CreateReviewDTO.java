package softtech.server.dto.ReviewDTO;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewDTO {

    @NotBlank(message = "Movie ID is required")
    private String movieId;

    @NotBlank(message = "Review content is required")
    @Size(min = 10, max = 1000, message = "Review must be between 10 and 1000 characters")
    private String content;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 10, message = "Rating must be at most 10")
    private Double rating;
}
