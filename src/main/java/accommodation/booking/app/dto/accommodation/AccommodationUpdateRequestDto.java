package accommodation.booking.app.dto.accommodation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record AccommodationUpdateRequestDto(
        List<Long> amenityIds,
        @Positive
        BigDecimal dailyRate,
        @Min(value = 0, message = "availability must be 0 (not available) or 1 (available)")
        @Max(value = 1, message = "availability must be 0 (not available) or 1 (available)")
        Integer availability) {
}
