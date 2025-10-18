package softtech.server.dto.BookingDTO;

import lombok.Data;

@Data
public class ShowtimeRequestDTO {
    private String movieId;
    private String roomId;
    private String startTime;
    private String language;
}