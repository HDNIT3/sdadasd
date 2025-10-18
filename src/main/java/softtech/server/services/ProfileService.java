package softtech.server.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import softtech.server.dto.ProfileDTO.FavoriteDTO;

import softtech.server.dto.ProfileDTO.ProfileDTO;
import softtech.server.dto.ProfileDTO.UpdateProfileSocialRequest;
import softtech.server.models.Account;
import softtech.server.models.Avatar;
import softtech.server.models.Customer;
import softtech.server.models.Employee;
import softtech.server.models.Movie;
import softtech.server.models.Review;
import softtech.server.repositories.AccountRepo;
import softtech.server.repositories.AvatarRepo;
import softtech.server.repositories.CustomerRepo;
import softtech.server.repositories.EmployeeRepo;
import softtech.server.repositories.ReviewRepo;

@Service
public class ProfileService {

    @Autowired private AvatarRepo avatarRepo;
    @Autowired private AccountRepo accountRepo;
    @Autowired private CustomerRepo customerRepo;
    @Autowired private EmployeeRepo employeeRepo;
    @Autowired private ReviewRepo reviewRepo;

    @Transactional(readOnly = true)
    public ProfileDTO getProfileByAccountIdOrAnonymous(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return guestProfile();
        }
        Account acc = accountRepo.findById(accountId).orElse(null);
        if (acc == null) {
            return guestProfile();
        }
        return buildProfileFromAccount(acc);
    }

    private ProfileDTO guestProfile() {
        return ProfileDTO.builder()
                .fullName(null)
                .email(null)
                .phone(null)
                .role(null)
                .avatarUrl(null)
                .facebookUrl(null)
                .instagramUrl(null)
                .twitterUrl(null)
                .linkedInUrl(null)
                .favorites(Collections.emptyList())
                .build();
    }

    private ProfileDTO buildProfileFromAccount(Account acc) {
        String role = acc.getRole() == null ? null : acc.getRole().name();
        String accountId = acc.getAccountId();

        String avatarUrl = null;
        Avatar a = avatarRepo.findByAccountId(accountId);
        if (a != null) {
            avatarUrl = a.getImageUrl();
        }

        String fullName = null;
        String email = null;
        String phone = null;

        if ("CUSTOMER".equalsIgnoreCase(role)) {
            Customer c = customerRepo.findByAccount_AccountId(accountId);
            if (c != null) {
                fullName = c.getFullName();
                email    = c.getEmail();
                phone    = c.getPhoneNumber();
            }
        } else {
            Employee e = employeeRepo.findByAccount_AccountId(accountId);
            if (e != null) {
                fullName = e.getFullName();
                email    = e.getEmail();
                phone    = e.getPhoneNumber();
            }
        }

        List<FavoriteDTO> favorites = Collections.emptyList();
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            favorites = getFavoriteMoviesByAccountIdOrEmpty(accountId, 5);
        }

        String facebookUrl  = acc.getFacebookUrl();
        String instagramUrl = acc.getInstagramUrl();
        String twitterUrl   = acc.getTwitterUrl();
        String linkedInUrl  = acc.getLinkedInUrl();

        return ProfileDTO.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .role(role)
                .avatarUrl(avatarUrl)
                .facebookUrl(facebookUrl)
                .instagramUrl(instagramUrl)
                .twitterUrl(twitterUrl)
                .linkedInUrl(linkedInUrl)
                .favorites(favorites)
                .build();
    }

    @Transactional(readOnly = true)
    public String getAvatarByAccountIdOrNull(String accountId) {
        if (accountId == null || accountId.isBlank()) return null;
        Avatar a = avatarRepo.findByAccountId(accountId);
        return a != null ? a.getImageUrl() : null;
    }

    @Transactional
    public String updateAvatarByAccountId(String accountId, String newUrl) {
        if (accountId == null || accountId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu accountId");
        }
        if (newUrl == null || newUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "imageUrl không được rỗng");
        }
        if (!accountRepo.existsById(accountId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy accountId: " + accountId);
        }

        Avatar existing = avatarRepo.findByAccountId(accountId);
        if (existing == null) {
            Avatar a = new Avatar();
            a.setAccountId(accountId);
            a.setImageUrl(newUrl);
            avatarRepo.save(a);
            return a.getImageUrl();
        } else {
            existing.setImageUrl(newUrl);
            avatarRepo.save(existing);
            return existing.getImageUrl();
        }
    }

    @Transactional(readOnly = true)
    public List<FavoriteDTO> getFavoriteMoviesByAccountIdOrEmpty(String accountId, int limit) {
        if (accountId == null || accountId.isBlank()) return Collections.emptyList();

        Customer c = customerRepo.findByAccount_AccountId(accountId);
        if (c == null) return Collections.emptyList();

        List<Review> reviews = reviewRepo.findByCustomer_CustomerId(c.getCustomerId());
        if (reviews == null || reviews.isEmpty()) return Collections.emptyList();

        reviews.sort(
                Comparator.comparingDouble((Review r) -> {
                    Double rate = r.getRating();
                    return rate != null ? rate.doubleValue() : 0.0d;
                }).reversed()
        );

        if (limit > 0 && reviews.size() > limit) {
            reviews = reviews.subList(0, Math.min(limit, reviews.size()));
        }

        List<FavoriteDTO> list = new ArrayList<>();
        for (Review r : reviews) {
            Movie m = r.getMovie();

            String movieId = null;
            String title   = null;
            Double rating  = r.getRating();

            if (m != null) {
                movieId = m.getMovieId();
                title   = m.getTitle();
            }

            list.add(FavoriteDTO.builder()
                    .movieId(movieId)
                    .title(title)
                    .rating(rating)
                    .reviewCount(null)
                    .build());
        }

        return list;
    }

    @Transactional
    public ProfileDTO updateFullNameAndSocial(UpdateProfileSocialRequest req) {
        if (req == null || req.getAccountId() == null || req.getAccountId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu accountId");
        }

        Account acc = accountRepo.findById(req.getAccountId()).orElse(null);
        if (acc == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Không tìm thấy accountId: " + req.getAccountId()
            );
        }

        if (acc.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found for accountId: " + req.getAccountId());
        }

        String role = acc.getRole().name();
        String accountId = acc.getAccountId();

        if ("CUSTOMER".equalsIgnoreCase(role)) {
            Customer c = customerRepo.findByAccount_AccountId(accountId);
            if (c == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Customer");
            c.setFullName(req.getFullName());
            customerRepo.save(c);
        } else {
            Employee e = employeeRepo.findByAccount_AccountId(accountId);
            if (e == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Employee");
            e.setFullName(req.getFullName());
            employeeRepo.save(e);
        }

        if (req.getFacebookUrl()  != null) acc.setFacebookUrl(req.getFacebookUrl());
        if (req.getInstagramUrl() != null) acc.setInstagramUrl(req.getInstagramUrl());
        if (req.getTwitterUrl()   != null) acc.setTwitterUrl(req.getTwitterUrl());
        if (req.getLinkedInUrl()  != null) acc.setLinkedInUrl(req.getLinkedInUrl());
        accountRepo.save(acc);

        return buildProfileFromAccount(acc);
    }
}