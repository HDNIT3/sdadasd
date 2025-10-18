package softtech.server.dto.BookingDTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SeatSelectDTO {
    private String seatId;
    private String position;
    private String status;
    private double price;
    private String type;
    private LocalDate lastUpdate;
    private String lockedBy;
}
