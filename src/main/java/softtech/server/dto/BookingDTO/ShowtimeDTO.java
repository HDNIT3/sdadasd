package softtech.server.dto.BookingDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShowtimeDTO {
    private String showtimeId;
    private String startTime;
    private String roomName;
    private String language;
}
