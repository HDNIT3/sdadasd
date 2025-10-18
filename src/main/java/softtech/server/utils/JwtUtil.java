package softtech.server.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private final ObjectMapper mapper = new ObjectMapper();

    public String generateToken(String username, String role, String userId) {
        try {
            long now = System.currentTimeMillis();
            long exp = now + expiration;

            System.out.println(secret);
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String payload = String.format(
                    "{\"sub\":\"%s\",\"role\":\"%s\",\"user_id\":\"%s\",\"iat\":%d,\"exp\":%d}",
                    username, role, userId, now / 1000, exp / 1000
            );

            String headerBase64 = base64UrlEncode(header.getBytes(StandardCharsets.UTF_8));
            String payloadBase64 = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));

            String signature = sign(headerBase64 + "." + payloadBase64, secret);

            String token = headerBase64 + "." + payloadBase64 + "." + signature;
            System.out.println("🎫 Generated token: " + token);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    public void verifyToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("Invalid token format");
            }

            // Kiểm tra chữ ký
            String signatureCheck = sign(parts[0] + "." + parts[1], secret);
            if (!signatureCheck.equals(parts[2])) {
                throw new RuntimeException("Invalid token signature");
            }

            // Kiểm tra expiration
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payloadMap = mapper.readValue(payloadJson, Map.class);

            long exp = ((Number) payloadMap.get("exp")).longValue();
            long now = System.currentTimeMillis() / 1000;
            if (now > exp) {
                throw new RuntimeException("Token expired");
            }

        } catch (Exception e) {
            throw new RuntimeException("Token verification failed: " + e.getMessage(), e);
        }
    }

    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payloadMap = mapper.readValue(payloadJson, Map.class);

            return (String) payloadMap.get("sub");
        } catch (Exception e) {
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payloadMap = mapper.readValue(payloadJson, Map.class);

            return (String) payloadMap.get("role");
        } catch (Exception e) {
            return null;
        }
    }

    public String extractUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payloadMap = mapper.readValue(payloadJson, Map.class);

            return (String) payloadMap.get("user_id");
        } catch (Exception e) {
            return null;
        }
    }

    // Ký HMAC SHA256
    private String sign(String data, String secret) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(keySpec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error signing token", e);
        }
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
