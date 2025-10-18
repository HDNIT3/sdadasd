package softtech.server.dto.ShowtimeDTO;

import lombok.Data;

import java.util.List;

@Data
public class RoomScheduleDTO {
    private String roomId;
    private List<String> movieIds;
    private Double occupancyRate;
}
