package softtech.server.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import softtech.server.dto.BookingDTO.SaveMovieRequest;
import softtech.server.dto.MovieDTO.MovieDTO;
import softtech.server.dto.ReviewDTO.ReviewDTO;
import softtech.server.dto.BookingDTO.ShowtimeDTO;
import softtech.server.enums.AgeRating;
import softtech.server.enums.Genre;
import softtech.server.models.Movie;
import softtech.server.models.Review;
import softtech.server.repositories.MovieRepo;
import softtech.server.repositories.ReviewRepo;
import softtech.server.repositories.ShowtimeRepo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private ShowtimeRepo showtimeRepo;

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private CloudinaryService cloudinaryService;

    public List<MovieDTO> getHotMovies() {
        List<Movie> movies = movieRepo.findByRatingGreaterThanEqual(7.0);
        return mapToDTO(movies);
    }

    public List<MovieDTO> getUpcomingMovies() {
        LocalDate today = LocalDate.now();
        List<Movie> movies = movieRepo.findByReleaseDateAfter(today);
        return mapToDTO(movies);
    }

    @Transactional
    public List<MovieDTO> getAllMovies() {
        List<Movie> movies = movieRepo.findAll();
        return mapToDTO(movies);
    }

    @Transactional
    public MovieDTO getMovieById(String id) {
        Movie movie = movieRepo.findById(id).orElse(null);
        if (movie == null) return null;

        List<String> genres = movie.getGenres().stream()
                .map(Enum::name)
                .toList();

        List<String> languages = showtimeRepo.findDistinctLanguagesByMovieId(movie.getMovieId());

        List<ShowtimeDTO> showtimeDTOs = movie.getShowtimes().stream().map(st ->
                ShowtimeDTO.builder()
                        .showtimeId(st.getShowtimeId())
                        .startTime(st.getStartTime().toString())
                        .roomName(st.getRoom().getName())
                        .language(st.getLanguage())
                        .build()
        ).toList();

        List<ReviewDTO> reviewDTOS = reviewRepo.findByMovieId(movie.getMovieId())
                .stream()
                .map(rv -> ReviewDTO.builder()
                        .reviewId(rv.getReviewId())
                        .customerId(rv.getCustomer().getCustomerId())
                        .cusName(rv.getCustomer().getFullName())
                        .content(rv.getContent())
                        .rating(rv.getRating())
                        .createdAt(rv.getCreatedAt())
                        .build()
                ).toList();

        return MovieDTO.builder()
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .posterUrl(movie.getPosterUrl())
                .duration(movie.getDuration())
                .rating(movie.getRating())
                .genres(genres)
                .ageRating(movie.getAgeRating().name())
                .releaseDate(movie.getReleaseDate())
                .cast(movie.getActors())
                .languages(languages)
                .showtimes(showtimeDTOs)
                .reviews(reviewDTOS)
                .build();
    }


    @Transactional
    public MovieDTO createMovie(SaveMovieRequest request, MultipartFile posterFile) throws IOException {
        validateMovieRequest(request);

        CloudinaryService.UploadResult uploadResult =
                cloudinaryService.uploadAvatar256(posterFile, "movies/posters");

        AgeRating ageRating = AgeRating.valueOf(request.getAgeRating().toUpperCase());
        List<Genre> genres = request.getGenres().stream()
                .map(g -> Genre.valueOf(g.toUpperCase()))
                .collect(Collectors.toList());

        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .duration(request.getDuration())
                .ageRating(ageRating)
                .posterUrl(uploadResult.url256())
                .releaseDate(LocalDate.parse(request.getReleaseDate()))
                .rating(request.getRating() != null ? request.getRating() : 0.0)
                .actors(request.getCast())
                .genres(genres)
                .build();

        Movie savedMovie = movieRepo.save(movie);

        return convertToSimpleDTO(savedMovie);
    }

    @Transactional
    public MovieDTO updateMovie(String movieId, SaveMovieRequest request, MultipartFile posterFile) throws IOException {
        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found with id: " + movieId));

        String oldPosterPublicId = null;

        if (request != null) {
            if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
                movie.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                movie.setDescription(request.getDescription());
            }
            if (request.getDuration() != null && request.getDuration() > 0) {
                movie.setDuration(request.getDuration());
            }
            if (request.getAgeRating() != null) {
                try {
                    movie.setAgeRating(AgeRating.valueOf(request.getAgeRating().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid age rating: " + request.getAgeRating());
                }
            }
            if (request.getReleaseDate() != null) {
                movie.setReleaseDate(LocalDate.parse(request.getReleaseDate()));
            }
            if (request.getRating() != null) {
                movie.setRating(request.getRating());
            }
            if (request.getGenres() != null && !request.getGenres().isEmpty()) {
                List<Genre> genres = request.getGenres().stream()
                        .map(g -> Genre.valueOf(g.toUpperCase()))
                        .collect(Collectors.toList());
                movie.setGenres(genres);
            }
            if (request.getCast() != null) {
                movie.setActors(request.getCast());
            }
        }

        if (posterFile != null && !posterFile.isEmpty()) {
            String currentPosterUrl = movie.getPosterUrl();
            oldPosterPublicId = extractPublicIdFromUrl(currentPosterUrl);

            CloudinaryService.UploadResult uploadResult =
                    cloudinaryService.uploadAvatar256(posterFile, "movies/posters");
            movie.setPosterUrl(uploadResult.url256());
        }

        Movie updatedMovie = movieRepo.save(movie);

        if (oldPosterPublicId != null) {
            try {
                cloudinaryService.deleteImage(oldPosterPublicId);
            } catch (Exception e) {
                System.err.println("Failed to delete old poster: " + e.getMessage());
            }
        }

        return convertToSimpleDTO(updatedMovie);
    }

    @Transactional
    public void deleteMovie(String movieId) throws IOException {
        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found with id: " + movieId));

        String publicId = extractPublicIdFromUrl(movie.getPosterUrl());

        movieRepo.delete(movie);

        if (publicId != null) {
            try {
                cloudinaryService.deleteImage(publicId);
            } catch (Exception e) {
                System.err.println("Failed to delete poster from Cloudinary: " + e.getMessage());
            }
        }
    }

    private String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;

        try {

            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) return null;

            String afterUpload = url.substring(uploadIndex + 8); // +8 để skip "/upload/"

            int lastSlash = afterUpload.lastIndexOf('/');
            if (lastSlash == -1) return null;

            String withFolder = afterUpload.substring(0, afterUpload.lastIndexOf('.'));

            return withFolder;
        } catch (Exception e) {
            System.err.println("Failed to extract publicId from URL: " + url);
            return null;
        }
    }

    private void validateMovieRequest(SaveMovieRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getDuration() == null || request.getDuration() <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (request.getReleaseDate() == null) {
            throw new IllegalArgumentException("Release date is required");
        }
        try {
            AgeRating.valueOf(request.getAgeRating().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid age rating");
        }
        if (request.getGenres() == null || request.getGenres().isEmpty()) {
            throw new IllegalArgumentException("At least one genre is required");
        }
    }

    private MovieDTO convertToSimpleDTO(Movie movie) {
        return MovieDTO.builder()
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .posterUrl(movie.getPosterUrl())
                .duration(movie.getDuration())
                .rating(movie.getRating())
                .genres(movie.getGenres() != null
                        ? movie.getGenres().stream().map(Enum::name).toList()
                        : List.of())
                .ageRating(movie.getAgeRating() != null ? movie.getAgeRating().name() : null)
                .releaseDate(movie.getReleaseDate())
                .cast(movie.getActors())
                .build();
    }

    private List<MovieDTO> mapToDTO(List<Movie> movies) {
        return movies.stream().map(movie -> {
            List<String> languages = showtimeRepo.findDistinctLanguagesByMovieId(movie.getMovieId());
            return MovieDTO.builder()
                    .movieId(movie.getMovieId())
                    .title(movie.getTitle())
                    .posterUrl(movie.getPosterUrl())
                    .duration(movie.getDuration())
                    .rating(movie.getRating())
                    .genres(movie.getGenres() != null
                            ? movie.getGenres().stream().map(Enum::name).toList()
                            : List.of())
                    .ageRating(movie.getAgeRating() != null ? movie.getAgeRating().name() : null)
                    .releaseDate(movie.getReleaseDate())
                    .cast(movie.getActors())
                    .languages(languages)
                    .build();
        }).toList();
    }

}