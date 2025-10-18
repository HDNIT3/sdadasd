package softtech.server.models;

import jakarta.persistence.*;
import lombok.*;
import softtech.server.enums.AgeRating;
import softtech.server.enums.Genre;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String movieId;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int duration;

    @Enumerated(EnumType.STRING)
    private AgeRating ageRating;

    @Column(nullable = false)
    private String posterUrl;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate releaseDate;

    @Column
    private Double rating;

    @ElementCollection
    @CollectionTable(
            name = "movie_actors",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "actor")
    private List<String> actors;

    @ElementCollection(targetClass = Genre.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "genre")
    private List<Genre> genres;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Showtime> showtimes = new ArrayList<>();
}
