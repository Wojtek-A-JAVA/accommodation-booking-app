package accommodation.booking.app.service;

import accommodation.booking.app.dto.location.LocationDto;
import java.util.List;

public interface LocationService {

    LocationDto getLocation(Long id);

    List<LocationDto> getLocations();
}
