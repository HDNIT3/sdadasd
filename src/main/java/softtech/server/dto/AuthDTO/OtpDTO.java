package softtech.server.dto.AuthDTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OtpDTO {
    private String code;
    private LocalDateTime expiresAt;
    private RegisterRequest registerRequest;

    public OtpDTO(String code, LocalDateTime expiresAt, RegisterRequest registerRequest) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.registerRequest = registerRequest;
    }
}