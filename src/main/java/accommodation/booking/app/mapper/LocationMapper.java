package accommodation.booking.app.mapper;

import accommodation.booking.app.config.MapperConfig;
import accommodation.booking.app.dto.location.LocationDto;
import accommodation.booking.app.model.Location;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface LocationMapper {
    LocationDto toDto(Location location);
}
