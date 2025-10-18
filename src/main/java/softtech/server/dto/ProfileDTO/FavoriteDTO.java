// File: src/main/java/softtech/server/dto/FavoriteDTO.java
// Mục đích: DTO cho 1 phim “ưa thích” hiển thị ở Profile.

package softtech.server.dto.ProfileDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FavoriteDTO {
    private String movieId;
    private String title;
    private Double rating;
    private Integer reviewCount;
}
