package softtech.server.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cloudinary", name = "enabled", havingValue = "true")
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final long MAX_SIZE_BYTES = 6L * 1024 * 1024; // 6MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/gif"
    );

    public record UploadResult(String publicId, String url256) {}

    public UploadResult uploadAvatar256(MultipartFile file, String folder) throws IOException {
        validate(file);

        Map upload = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "overwrite", false,
                "unique_filename", true
        ));

        String publicId = (String) upload.get("public_id");

        String url256 = cloudinary.url()
                .transformation(new Transformation().width(256).height(256).crop("fill").gravity("auto"))
                .generate(publicId);

        return new UploadResult(publicId, url256);
    }

    public void deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.isEmpty()) {
            throw new IllegalArgumentException("PublicId cannot be null or empty");
        }

        Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

        String resultStatus = (String) result.get("result");
        if (!"ok".equals(resultStatus) && !"not found".equals(resultStatus)) {
            throw new IOException("Failed to delete image from Cloudinary. Status: " + resultStatus);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("File too large. Max 6MB");
        }
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_TYPES.contains(ct)) {
            throw new IllegalArgumentException("Invalid image type. Only JPG/PNG/GIF");
        }
    }
}