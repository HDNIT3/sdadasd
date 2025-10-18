package softtech.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import softtech.server.dto.BookingDTO.SaveMovieRequest;
import softtech.server.dto.MovieDTO.MovieDTO;
import softtech.server.services.MovieService;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "http://localhost:3000")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping("/hot")
    public ResponseEntity<?> getHotMovies() {
        List<MovieDTO> movies = movieService.getHotMovies();
        if (movies.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No movies found");
        }
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingMovies() {
        List<MovieDTO> movies = movieService.getUpcomingMovies();
        if (movies.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No movies found");
        }
        return ResponseEntity.ok(movies);
    }

    @GetMapping
    public ResponseEntity<?> getAllMovies() {
        List<MovieDTO> movies = movieService.getAllMovies();
        if (movies.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No movies found");
        }
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<?> getMovieById(@PathVariable String movieId) {
        MovieDTO movie = movieService.getMovieById(movieId);
        if (movie == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Movie not found with id: " + movieId);
        }
        return ResponseEntity.ok(movie);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createMovie(
            @RequestPart("movie") SaveMovieRequest request,
            @RequestPart("poster") MultipartFile posterFile
    ) {
        try {
            MovieDTO createdMovie = movieService.createMovie(request, posterFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating movie: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{movieId}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateMovie(
            @PathVariable String movieId,
            @RequestPart(value = "movie", required = false) SaveMovieRequest request,
            @RequestPart(value = "poster", required = false) MultipartFile posterFile
    ) {
        try {
            if (request == null && posterFile == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Either movie data or poster must be provided");
            }

            MovieDTO updatedMovie = movieService.updateMovie(movieId, request, posterFile);
            return ResponseEntity.ok(updatedMovie);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating movie: " + e.getMessage());
        }
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<?> deleteMovie(@PathVariable String movieId) {
        try {
            movieService.deleteMovie(movieId);

            return ResponseEntity.ok("Movie deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting movie: " + e.getMessage());
        }
    }
}