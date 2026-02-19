package accommodation.booking.app.mapper;

import accommodation.booking.app.config.MapperConfig;
import accommodation.booking.app.dto.booking.BookingDto;
import accommodation.booking.app.dto.booking.CreateBookingRequestDto;
import accommodation.booking.app.model.Accommodation;
import accommodation.booking.app.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface BookingMapper {

    @Mapping(target = "accommodation", source = "accommodationId")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    Booking toEntity(CreateBookingRequestDto requestDto);

    @Mapping(target = "accommodationId", source = "accommodation.id")
    @Mapping(target = "userId", source = "user.id")
    BookingDto toDto(Booking booking);

    default Accommodation mapIdToAccommodation(Long accommodationId) {
        if (accommodationId == null) {
            return null;
        }
        return new Accommodation().setId(accommodationId);
    }
}
