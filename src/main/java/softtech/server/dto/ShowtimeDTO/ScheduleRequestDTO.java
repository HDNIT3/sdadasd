package softtech.server.dto.ShowtimeDTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ScheduleRequestDTO {
    private LocalDate date;
    private List<RoomScheduleDTO> roomSchedules;
}


