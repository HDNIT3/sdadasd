package softtech.server.dto.ProfileDTO;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponseDTO {
    private List<RecommendationItemDTO> byTopGenre;
    private List<RecommendationItemDTO> byTopActor;
}
