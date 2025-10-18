package softtech.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softtech.server.dto.AuthDTO.AuthRequest;
import softtech.server.services.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "https://zzzzz-production.up.railway.app")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> loginAccount(@RequestBody AuthRequest request) {
        String token = authService.login(request);

        if (token == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Username or password are incorrect")
            );
        }

        return ResponseEntity.ok(Map.of(
                "token", token,
                "message", "Login successful"
        ));
    }
}