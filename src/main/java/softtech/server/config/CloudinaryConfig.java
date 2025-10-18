package softtech.server.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "cloudinary", name = "enabled", havingValue = "true")
public class CloudinaryConfig {

    @Value("${cloudinary.cloud_name:${CLOUDINARY_CLOUD_NAME:}}")
    private String cloud;

    @Value("${cloudinary.api_key:${CLOUDINARY_API_KEY:}}")
    private String key;

    @Value("${cloudinary.api_secret:${CLOUDINARY_API_SECRET:}}")
    private String secret;

    @Bean
    public Cloudinary cloudinary() {
        if (cloud == null || cloud.isBlank() || key == null || key.isBlank() || secret == null || secret.isBlank()) {
            throw new IllegalStateException("Cloudinary config missing (cloud_name/api_key/api_secret).");
        }
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloud,
                "api_key", key,
                "api_secret", secret,
                "secure", true
        ));
    }
}
