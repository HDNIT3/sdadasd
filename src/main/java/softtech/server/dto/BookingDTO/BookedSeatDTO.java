package softtech.server.dto.BookingDTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookedSeatDTO {
    private String seatId;
    private String position;
    private double price;
    private String seatType;
}