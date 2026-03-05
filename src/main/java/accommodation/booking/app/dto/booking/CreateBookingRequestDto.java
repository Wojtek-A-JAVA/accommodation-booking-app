package accommodation.booking.app.dto.booking;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateBookingRequestDto(
        @NotNull
        @FutureOrPresent
        LocalDate checkInDate,
        @NotNull
        @FutureOrPresent
        LocalDate checkOutDate,
        @NotNull
        @Positive
        Long accommodationId
) {
    @AssertTrue(message = "checkOutDate must be after checkInDate")
    boolean isCheckOutDateAfterCheckInDate() {
        return checkInDate == null || checkOutDate == null || checkOutDate.isAfter(checkInDate);
    }
}
