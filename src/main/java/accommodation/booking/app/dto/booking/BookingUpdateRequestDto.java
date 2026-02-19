package accommodation.booking.app.dto.booking;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

public record BookingUpdateRequestDto(
        @FutureOrPresent
        LocalDate checkInDate,
        @FutureOrPresent
        LocalDate checkOutDate,
        String status
) {
    @AssertTrue(message = "checkOutDate must be after checkInDate")
    boolean isCheckOutDateAfterCheckInDate() {
        return checkInDate == null || checkOutDate == null || checkOutDate.isAfter(checkInDate);
    }
}
