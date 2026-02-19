package accommodation.booking.app.dto.accommodation;

import accommodation.booking.app.model.Type;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record CreateAccommodationRequestDto(
        @NotNull
        Type type,
        @NotNull
        @Positive
        Long locationId,
        @NotNull
        String size,
        @NotNull
        List<Long> amenityIds,
        @NotNull
        @Positive
        BigDecimal dailyRate,
        @NotNull
        @Min(value = 0, message = "availability must be 0 (not available) or 1 (available)")
        @Max(value = 1, message = "availability must be 0 (not available) or 1 (available)")
        Integer availability
) {
}
