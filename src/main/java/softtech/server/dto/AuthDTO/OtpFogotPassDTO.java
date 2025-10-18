package softtech.server.dto.AuthDTO;

import java.time.LocalDateTime;

import lombok.*;

@Data
public class OtpFogotPassDTO {
	private String code;
    private LocalDateTime expiresAt;
    private FogotPassDTO fogotPassDTO;
    
    public OtpFogotPassDTO(String code, LocalDateTime expiresAt, FogotPassDTO fogotPassDTO) {
		this.code = code;
		this.expiresAt = expiresAt;
		this.fogotPassDTO = fogotPassDTO;
	}
}
