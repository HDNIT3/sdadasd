package softtech.server.dto.ProfileDTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationItemDTO {
    private String movieId;
    private String title;
    private Double avgRating;   // trung bình đánh giá hệ thống
    private String releaseDate; // yyyy-MM-dd (null nếu không có)
    private String reason;      // "top-genre: DRAMA" | "top-actor: Tom Hanks"
}
