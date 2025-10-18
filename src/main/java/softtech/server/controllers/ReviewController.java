package softtech.server.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softtech.server.dto.ReviewDTO.ReviewDTO;
import softtech.server.dto.ReviewDTO.CreateReviewDTO;
import softtech.server.repositories.CustomerRepo;
import softtech.server.services.ReviewService;
import softtech.server.utils.JwtUtil;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "https://zzzzz-production.up.railway.app")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;
    private final CustomerRepo customerRepo;

    /**
     * Lấy danh sách reviews với filter và pagination
     */
    @GetMapping
    public ResponseEntity<?> getReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String movieTitle,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        try {
            LocalDate fromDate = (from != null && !from.isEmpty()) ? LocalDate.parse(from) : null;
            LocalDate toDate = (to != null && !to.isEmpty()) ? LocalDate.parse(to) : null;

            List<ReviewDTO> reviews = reviewService.getFilteredReviews(page, size, movieTitle, fromDate, toDate);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error fetching reviews: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Lấy review theo ID
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable String reviewId) {
        try {
            ReviewDTO review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error fetching review: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Tạo review mới (yêu cầu authentication)
     */
    @PostMapping
    public ResponseEntity<?> createReview(
            @Valid @RequestBody CreateReviewDTO createDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Missing or invalid authorization header\"}");
            }

            String token = authHeader.substring(7);
            String accountId = jwtUtil.extractUserId(token);
            String customerId = customerRepo.findByAccount_AccountId(accountId).getCustomerId();

            ReviewDTO review = reviewService.createReview(createDTO, customerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error creating review: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Cập nhật review (chỉ người tạo mới có thể sửa)
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable String reviewId,
            @Valid @RequestBody CreateReviewDTO updateDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Missing or invalid authorization header\"}");
            }

            String token = authHeader.substring(7);
            String accountId = jwtUtil.extractUserId(token);
            String customerId = customerRepo.findByAccount_AccountId(accountId).getCustomerId();

            ReviewDTO review = reviewService.updateReview(reviewId, updateDTO, customerId);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("only update your own")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"error\": \"" + e.getMessage() + "\"}");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error updating review: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Xóa review (chỉ người tạo mới có thể xóa)
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable String reviewId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Missing or invalid authorization header\"}");
            }

            String token = authHeader.substring(7);
            String accountId = jwtUtil.extractUserId(token);
            String customerId = customerRepo.findByAccount_AccountId(accountId).getCustomerId();

            reviewService.deleteReview(reviewId, customerId);
            return ResponseEntity.ok("{\"message\": \"Review deleted successfully\"}");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("only delete your own")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"error\": \"" + e.getMessage() + "\"}");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error deleting review: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Lấy tất cả reviews của một phim cụ thể
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<?> getReviewsByMovie(@PathVariable String movieId) {
        try {
            List<ReviewDTO> reviews = reviewService.getReviewsByMovieId(movieId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error fetching movie reviews: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Lấy reviews của customer hiện tại
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Missing or invalid authorization header\"}");
            }

            String token = authHeader.substring(7);
            String accountId = jwtUtil.extractUserId(token);
            String customerId = customerRepo.findByAccount_AccountId(accountId).getCustomerId();

            List<ReviewDTO> reviews = reviewService.getReviewsByCustomerId(customerId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error fetching your reviews: " + e.getMessage() + "\"}");
        }
    }
}