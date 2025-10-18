
package softtech.server.dto.ProfileDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProfileDTO {

    // Thông tin cơ bản
    private String fullName;
    private String email;
    private String phone;
    private String role;

    // Ảnh đại diện
    private String avatarUrl;

    // Mạng xã hội (placeholder)
    private String facebookUrl;
    private String instagramUrl;
    private String twitterUrl;
    private String linkedInUrl;

    // Phim ưa thích (top N)
    private List<FavoriteDTO> favorites;
    
    
}
