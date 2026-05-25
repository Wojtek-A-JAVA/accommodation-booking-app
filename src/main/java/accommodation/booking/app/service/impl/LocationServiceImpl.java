package accommodation.booking.app.service.impl;

import accommodation.booking.app.dto.location.LocationDto;
import accommodation.booking.app.exception.EntityNotFoundException;
import accommodation.booking.app.mapper.LocationMapper;
import accommodation.booking.app.model.Location;
import accommodation.booking.app.repository.LocationRepository;
import accommodation.booking.app.service.LocationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    public LocationDto getLocation(Long id) {
        Location location = locationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Location with id: " + id
                        + " not found in data base")
        );
        return locationMapper.toDto(location);
    }

    @Override
    public List<LocationDto> getLocations() {
        List<Location> locationList = locationRepository.findAll();
        return locationList.stream()
                .map(locationMapper::toDto)
                .toList();
    }
}
