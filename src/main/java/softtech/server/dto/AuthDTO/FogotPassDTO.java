package softtech.server.dto.AuthDTO;

import lombok.Data;

@Data
public class FogotPassDTO {
	private String email;
	private String otp;
	private String newPassword;
}
