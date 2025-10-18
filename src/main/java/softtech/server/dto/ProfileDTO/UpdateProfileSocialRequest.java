package softtech.server.dto.ProfileDTO;

import lombok.Data;

@Data
public class UpdateProfileSocialRequest {
    private String accountId;       // bắt buộc

    // chỉ cho phép đổi các field sau:
    private String fullName;        // đổi tên hiển thị

    private String facebookUrl;     // 4 link social
    private String instagramUrl;
    private String twitterUrl;
    private String linkedInUrl;
}
