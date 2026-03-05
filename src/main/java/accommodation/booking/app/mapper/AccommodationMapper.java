package accommodation.booking.app.mapper;

import accommodation.booking.app.config.MapperConfig;
import accommodation.booking.app.dto.accommodation.AccommodationDto;
import accommodation.booking.app.dto.accommodation.CreateAccommodationRequestDto;
import accommodation.booking.app.model.Accommodation;
import accommodation.booking.app.model.Amenity;
import accommodation.booking.app.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface AccommodationMapper {

    @Mapping(target = "amenities", source = "amenityIds")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", source = "locationId")
    Accommodation toEntity(CreateAccommodationRequestDto requestDto);

    @Mapping(target = "amenityIds", source = "amenities")
    @Mapping(target = "locationId", source = "location.id")
    AccommodationDto toDto(Accommodation accommodation);

    default Amenity mapIdToAmenities(Long id) {
        if (id == null) {
            return null;
        }
        return new Amenity().setId(id);
    }

    default Long mapAmenitiesToId(Amenity amenity) {
        return (amenity == null) ? null : amenity.getId();
    }

    default Location mapIdToLocation(Long locationId) {
        if (locationId == null) {
            return null;
        }
        return new Location().setId(locationId);
    }
}
