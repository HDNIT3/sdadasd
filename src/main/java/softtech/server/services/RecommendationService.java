package softtech.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softtech.server.dto.ProfileDTO.RecommendationItemDTO;
import softtech.server.dto.ProfileDTO.RecommendationResponseDTO;
import softtech.server.enums.Genre;
import softtech.server.models.Customer;
import softtech.server.models.Movie;
import softtech.server.models.Review;
import softtech.server.repositories.CustomerRepo;
import softtech.server.repositories.MovieRepo;
import softtech.server.repositories.ReviewRepo;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final CustomerRepo customerRepo;
    private final ReviewRepo reviewRepo;
    private final MovieRepo movieRepo;

    @Transactional(readOnly = true)
    public RecommendationResponseDTO recommendByTopGenreAndActor(String accountId, int k) {
        // 1) accountId -> customer -> reviews
        Customer c = customerRepo.findByAccount_AccountId(accountId);
        if (c == null) {
            return RecommendationResponseDTO.builder()
                    .byTopGenre(Collections.emptyList())
                    .byTopActor(Collections.emptyList())
                    .build();
        }
        List<Review> reviews = reviewRepo.findByCustomer_CustomerId(c.getCustomerId());
        if (reviews == null || reviews.isEmpty()) {
            return RecommendationResponseDTO.builder()
                    .byTopGenre(Collections.emptyList())
                    .byTopActor(Collections.emptyList())
                    .build();
        }

        // Lấy tập phim đã review
        Set<Movie> reviewedMovies = reviews.stream()
                .map(Review::getMovie)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2) Đếm tần suất genre & actor
        Map<Genre, Long> genreCount = new HashMap<>();
        Map<String, Long> actorCount = new HashMap<>();

        for (Movie m : reviewedMovies) {
            if (m.getGenres() != null) {
                m.getGenres().forEach(g -> genreCount.merge(g, 1L, Long::sum));
            }
            if (m.getActors() != null) {
                m.getActors().forEach(a -> {
                    if (a != null && !a.isBlank()) {
                        actorCount.merge(a, 1L, Long::sum);
                    }
                });
            }
        }

        Genre topGenre = genreCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);

        String topActor = actorCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);

        // 3) Lấy phim theo topGenre / topActor
        List<Movie> byGenre = (topGenre == null)
                ? Collections.emptyList()
                : movieRepo.findByGenresJoin(topGenre);

        List<Movie> byActor = (topActor == null)
                ? Collections.emptyList()
                : movieRepo.findByActorsJoin(topActor);

        // 4) Xếp hạng & lấy top K
        List<RecommendationItemDTO> topByGenre = rank(byGenre, "top-genre: " + (topGenre == null ? "N/A" : topGenre.name()), k);
        List<RecommendationItemDTO> topByActor = rank(byActor, "top-actor: " + (topActor == null ? "N/A" : topActor), k);

        return RecommendationResponseDTO.builder()
                .byTopGenre(topByGenre)
                .byTopActor(topByActor)
                .build();
    }

    private List<RecommendationItemDTO> rank(List<Movie> movies, String reason, int k) {
        if (movies == null || movies.isEmpty()) return Collections.emptyList();

        List<RecommendationItemDTO> list = new ArrayList<>(movies.size());
        for (Movie m : movies) {
            list.add(RecommendationItemDTO.builder()
                    .movieId(m.getMovieId())
                    .title(m.getTitle())
                    .avgRating(m.getRating())                     // ✅ lấy trực tiếp rating từ bảng movies
                    .releaseDate(m.getReleaseDate().toString())   // ✅ luôn có giá trị
                    .reason(reason)
                    .build());
        }

        // ✅ Sắp xếp theo rating ↓, nếu bằng nhau thì ngày phát hành ↓
        list.sort(
            Comparator.comparing(RecommendationItemDTO::getAvgRating).reversed()
                    .thenComparing(RecommendationItemDTO::getReleaseDate, Comparator.reverseOrder())
        );

        return list.size() > k ? list.subList(0, k) : list;
    }

}
