package accommodation.booking.app.dto.booking;

import accommodation.booking.app.model.Type;
import java.time.LocalDate;

public record BookingDto(
        Long id,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Long accommodationId,
        String city,
        String street,
        Type accommodationType,
        Long userId,
        String status
) {
}
