package softtech.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import softtech.server.dto.ProfileDTO.FavoriteDTO;
import softtech.server.models.Movie;
import softtech.server.models.Review;
import softtech.server.repositories.MovieRepo;
import softtech.server.repositories.ReviewRepo;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final ReviewRepo reviewRepo;
    private final MovieRepo movieRepo;

    public List<FavoriteDTO> getTopFavorites(String accountId, int limit) {
        int top = Math.max(1, limit);

        List<Review> reviews = reviewRepo.findByCustomer_CustomerId(accountId);
        if (reviews.isEmpty()) return Collections.emptyList();

        Map<String, Double> bestRatingPerMovie = reviews.stream()
                .collect(Collectors.toMap(
                        r -> r.getMovie().getMovieId(),
                        Review::getRating,
                        Double::max
                ));

        Map<String, Long> reviewCountPerMovie = reviews.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getMovie().getMovieId(),
                        Collectors.counting()
                ));

        List<Movie> movies = movieRepo.findByMovieIdIn(new ArrayList<>(bestRatingPerMovie.keySet()));

        List<FavoriteDTO> all = movies.stream().map(m -> {
            String movieId = m.getMovieId();
            Double bestRating = bestRatingPerMovie.getOrDefault(movieId, 0.0);
            int reviewCount = reviewCountPerMovie.getOrDefault(movieId, 0L).intValue();

            return FavoriteDTO.builder()
                    .movieId(movieId)
                    .title(m.getTitle())
                    .rating(bestRating)
                    .reviewCount(reviewCount)
                    .build();
        }).collect(Collectors.toList());

        return all.stream()
                .sorted(Comparator.comparing(FavoriteDTO::getRating).reversed())
                .limit(top)
                .collect(Collectors.toList());
    }
}
