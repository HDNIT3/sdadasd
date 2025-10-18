package softtech.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import softtech.server.dto.AuthDTO.RegisterRequest;
import softtech.server.dto.AuthDTO.VerifyOtpRequest;
import softtech.server.dto.AuthDTO.FogotPassDTO;
import softtech.server.exceptions.ValidationException;
import softtech.server.models.Account;
import softtech.server.services.OtpService;

import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@CrossOrigin(origins = "http://localhost:3000")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody RegisterRequest request) {
        try {
            otpService.sendOtp(request);
            return ResponseEntity.ok(Map.of(
                    "message", "OTP sent successfully to " + request.getEmail(),
                    "email", request.getEmail()
            ));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("errors", e.getErrors()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Failed to send OTP",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtpAndRegister(@RequestBody VerifyOtpRequest request) {
        try {
            Account account = otpService.verifyOtpAndRegister(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Registration successful",
                    "username", account.getUsername(),
                    "role", account.getRole().toString()
            ));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("errors", e.getErrors()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Registration failed",
                    "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/forgot-password/send")
    public ResponseEntity<?> sendForgotPasswordOtp(@RequestBody FogotPassDTO request) {
        try {
            otpService.sendOtp_ForgetPass(request);
            return ResponseEntity.ok(Map.of(
                    "message", "OTP sent successfully to " + request.getEmail(),
                    "email", request.getEmail()
            ));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("errors", e.getErrors()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Failed to send OTP",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<?> verifyOtpAndResetPassword(@RequestBody FogotPassDTO request) {
        try {
            Boolean result = otpService.verifyOtpAndFogotPass(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successfully",
                    "success", result
            ));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("errors", e.getErrors()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Password reset failed",
                    "error", e.getMessage()
            ));
        }
    }

}