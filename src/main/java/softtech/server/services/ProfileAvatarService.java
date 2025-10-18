package softtech.server.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import softtech.server.models.Avatar;
import softtech.server.repositories.AvatarRepo;

import java.io.IOException;

@Service
public class ProfileAvatarService {

    @Autowired(required = false)
    private CloudinaryService cloudinaryService;
    
    private final AvatarRepo avatarRepo;

    public ProfileAvatarService(AvatarRepo avatarRepo) {
        this.avatarRepo = avatarRepo;
    }

    @Transactional
    public String uploadAvatar(String accountId, MultipartFile file) throws IOException {
        if (cloudinaryService == null) {
            throw new UnsupportedOperationException("Cloudinary service is not configured. Please set cloudinary.enabled=true and provide valid Cloudinary credentials.");
        }
        
        var result = cloudinaryService.uploadAvatar256(file, "avatars/" + accountId);

        Avatar avatar = avatarRepo.findByAccountId(accountId);
        if (avatar == null) {
            avatar = new Avatar();
            avatar.setAccountId(accountId);
        }

        avatar.setImageUrl(result.url256());
        avatarRepo.save(avatar);

        return avatar.getImageUrl();
    }
}