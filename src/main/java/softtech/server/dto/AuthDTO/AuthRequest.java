package softtech.server.dto.AuthDTO;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
