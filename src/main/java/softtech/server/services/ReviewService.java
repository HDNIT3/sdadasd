package softtech.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import softtech.server.dto.ReviewDTO.ReviewDTO;
import softtech.server.dto.ReviewDTO.CreateReviewDTO;
import softtech.server.models.Review;
import softtech.server.models.Movie;
import softtech.server.models.Customer;
import softtech.server.repositories.ReviewRepo;
import softtech.server.repositories.MovieRepo;
import softtech.server.repositories.CustomerRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private CustomerRepo customerRepo;

    public List<ReviewDTO> getFilteredReviews(int page, int size, String movieTitle, LocalDate from, LocalDate to) {
        Pageable pageable = PageRequest.of(page - 1, size);

        LocalDateTime fromDateTime = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = (to != null) ? to.plusDays(1).atStartOfDay() : null;

        List<Review> reviews = reviewRepo.findFilteredReviews(movieTitle, fromDateTime, toDateTime, pageable);

        return reviews.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ReviewDTO createReview(CreateReviewDTO createDTO, String customerId) {
        Movie movie = movieRepo.findById(createDTO.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + createDTO.getMovieId()));

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        Review review = new Review();
        review.setContent(createDTO.getContent());
        review.setRating(createDTO.getRating());
        review.setMovie(movie);
        review.setCustomer(customer);
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepo.save(review);

        updateMovieRating(movie.getMovieId());

        return convertToDTO(savedReview);
    }

    public ReviewDTO getReviewById(String reviewId) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
        return convertToDTO(review);
    }

    public ReviewDTO updateReview(String reviewId, CreateReviewDTO updateDTO, String customerId) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        if (!review.getCustomer().getCustomerId().equals(customerId)) {
            throw new RuntimeException("You can only update your own reviews");
        }

        review.setContent(updateDTO.getContent());
        review.setRating(updateDTO.getRating());

        Review updatedReview = reviewRepo.save(review);

        updateMovieRating(review.getMovie().getMovieId());

        return convertToDTO(updatedReview);
    }

    public void deleteReview(String reviewId, String customerId) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        if (!review.getCustomer().getCustomerId().equals(customerId)) {
            throw new RuntimeException("You can only delete your own reviews");
        }

        String movieId = review.getMovie().getMovieId();
        reviewRepo.delete(review);

        updateMovieRating(movieId);
    }

    private void updateMovieRating(String movieId) {
        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));

        Double averageRating = reviewRepo.calculateAverageRatingByMovieId(movieId);

        movie.setRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);

        movieRepo.save(movie);
    }

    public List<ReviewDTO> getReviewsByMovieId(String movieId) {
        List<Review> reviews = reviewRepo.findByMovieId(movieId);
        return reviews.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ReviewDTO> getReviewsByCustomerId(String customerId) {
        List<Review> reviews = reviewRepo.findByCustomerId(customerId);
        return reviews.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private ReviewDTO convertToDTO(Review r) {
        ReviewDTO dto = ReviewDTO.builder()
                .reviewId(r.getReviewId())
                .rating(r.getRating())
                .content(r.getContent())
                .createdAt(r.getCreatedAt())
                .build();

        if (r.getCustomer() != null) {
            dto.setCustomerId(r.getCustomer().getCustomerId());
            dto.setCusName(r.getCustomer().getFullName());
        }
        if (r.getMovie() != null) {
            dto.setMovieId(r.getMovie().getMovieId());
            dto.setMovieTitle(r.getMovie().getTitle());
        }
        return dto;
    }
}