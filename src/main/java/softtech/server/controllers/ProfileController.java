package softtech.server.controllers;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import softtech.server.dto.ProfileDTO.AvatarDTO;
import softtech.server.dto.ProfileDTO.FavoriteDTO;
import softtech.server.dto.ProfileDTO.ProfileDTO;
import softtech.server.dto.ProfileDTO.UpdateProfileSocialRequest;
import softtech.server.services.FavoriteService;
import softtech.server.services.ProfileAvatarService;
import softtech.server.services.ProfileService;

import softtech.server.services.RecommendationService;
import softtech.server.dto.ProfileDTO.RecommendationResponseDTO;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "https://zzzzz-production.up.railway.app")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final FavoriteService favoriteService;
    private final ProfileAvatarService profileAvatarService;
    
    private final RecommendationService recommendationService; // [ADD]

    @GetMapping("/{accountId}")
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable String accountId) {
        ProfileDTO dto = profileService.getProfileByAccountIdOrAnonymous(accountId);
        return ResponseEntity.ok(dto);
    }
    @PutMapping("/{accountId}")
    public ResponseEntity<ProfileDTO> updateProfile(
            @PathVariable String accountId,
            @RequestBody UpdateProfileSocialRequest request) {
        request.setAccountId(accountId);
        ProfileDTO updated = profileService.updateFullNameAndSocial(request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{accountId}/avatar")
    public ResponseEntity<AvatarDTO> getAvatar(@PathVariable String accountId) {
        String avatar = profileService.getAvatarByAccountIdOrNull(accountId);
        AvatarDTO dto = new AvatarDTO();
        dto.setAvatarUrl(avatar);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{accountId}/avatar")
    public ResponseEntity<AvatarDTO> updateAvatar(
            @PathVariable String accountId,
            @RequestParam("imageUrl") String imageUrl) {
        String newUrl = profileService.updateAvatarByAccountId(accountId, imageUrl);
        AvatarDTO dto = new AvatarDTO();
        dto.setAvatarUrl(newUrl);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{accountId}/favorites")
    public ResponseEntity<List<FavoriteDTO>> getFavorites(
            @PathVariable String accountId,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        List<FavoriteDTO> list = profileService.getFavoriteMoviesByAccountIdOrEmpty(accountId, limit);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{accountId}/favorites/top")
    public ResponseEntity<List<FavoriteDTO>> getTopFavorites(
            @PathVariable String accountId,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        return ResponseEntity.ok(favoriteService.getTopFavorites(accountId, limit));
    }

    @PutMapping(value = "/{accountId}/avatar", consumes = {"multipart/form-data"})
    public ResponseEntity<AvatarDTO> uploadAvatar(
            @PathVariable String accountId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String avatarUrl = profileAvatarService.uploadAvatar(accountId, file);
        AvatarDTO dto = new AvatarDTO();
        dto.setAvatarUrl(avatarUrl);
        return ResponseEntity.ok(dto);
    }
    
 // [ADD]
    @GetMapping("/{accountId}/recommendations/simple")
    public ResponseEntity<RecommendationResponseDTO> getSimpleRecommendations(
            @PathVariable String accountId,
            @RequestParam(name = "k", defaultValue = "3") int k) {
        return ResponseEntity.ok(recommendationService.recommendByTopGenreAndActor(accountId, k));
    }

}
