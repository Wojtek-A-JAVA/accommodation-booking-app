package accommodation.booking.app.dto.accommodation;

import accommodation.booking.app.model.Type;
import java.math.BigDecimal;
import java.util.List;

public record AccommodationDto(
        Long id,
        Type type,
        Long locationId,
        String size,
        List<Long> amenityIds,
        BigDecimal dailyRate,
        Integer availability
) {
}
