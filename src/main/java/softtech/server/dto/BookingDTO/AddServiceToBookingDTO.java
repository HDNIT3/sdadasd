package softtech.server.dto.BookingDTO;

import lombok.Data;
import softtech.server.models.Booking;
import softtech.server.models.ServiceOrder;

@Data
public class AddServiceToBookingDTO {
    private ServiceOrder serviceOrder;
    private Booking booking;
    private double totalPrice;
}
