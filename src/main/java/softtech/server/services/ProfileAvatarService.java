package softtech.server.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import softtech.server.models.Avatar;
import softtech.server.repositories.AvatarRepo;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProfileAvatarService {

    private final CloudinaryService cloudinaryService;
    private final AvatarRepo avatarRepo;

    @Transactional
    public String uploadAvatar(String accountId, MultipartFile file) throws IOException {
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
