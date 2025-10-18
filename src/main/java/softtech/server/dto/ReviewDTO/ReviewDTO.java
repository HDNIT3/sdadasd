package softtech.server.dto.ReviewDTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewDTO {
    private String reviewId;
    private String content;
    private double rating;

    private String customerId;
    private String cusName;
    private LocalDateTime createdAt;

    private String movieId;
    private String movieTitle;
}
