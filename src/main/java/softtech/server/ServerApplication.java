package softtech.server;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import softtech.server.dto.AuthDTO.RegisterRequest;
import softtech.server.dto.AuthDTO.VerifyOtpRequest;
import softtech.server.models.Account;
import softtech.server.services.OtpService;

import java.time.LocalDate;

@SpringBootApplication
public class ServerApplication {

    private final OtpService otpService;

    public ServerApplication(OtpService otpService) {
        this.otpService = otpService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @PostConstruct
    public void createSampleUser() {
        try {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("User");
            request.setPassword("User@123");
            request.setRole("CUSTOMER");
            request.setFullName("Sample User");
            request.setEmail("user@example.com");
            request.setPhoneNumber("0123456789");
            request.setDateOfBirth(LocalDate.of(2000, 1, 1));
            request.setBusinessCode(null); // để trống

            Account account = otpService.verifyOtpAndRegister(
                    new VerifyOtpRequest() {{
                        setEmail(request.getEmail());
                        setOtp("000000"); // OTP giả để bypass, hoặc bạn có thể tạo method riêng để tạo account trực tiếp
                    }}
            );

            System.out.println("Sample user created: " + account.getUsername());
        } catch (Exception e) {
            System.out.println("Sample user creation failed: " + e.getMessage());
        }
    }
}
