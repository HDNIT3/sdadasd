package softtech.server.dto.BookingDTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShowtimeResponseDTO {
    private String showtimeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String language;
    private RoomDTO room;
    private MovieDTO movie;

    @Data
    public static class RoomDTO {
        private String roomId;
        private String name;
        private Integer capacity;
    }

    @Data
    public static class MovieDTO {
        private String movieId;
        private String title;
        private Integer duration;
    }
}