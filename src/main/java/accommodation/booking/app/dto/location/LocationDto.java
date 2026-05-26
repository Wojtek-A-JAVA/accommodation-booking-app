package accommodation.booking.app.dto.location;

public record LocationDto(
        Long id,
        String country,
        String city,
        String street
) {
}
