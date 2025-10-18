package softtech.server.dto.AuthDTO;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email;
    private String otp;

    public VerifyOtpRequest() {
    }

    public VerifyOtpRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }
}