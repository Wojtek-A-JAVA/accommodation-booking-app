package accommodation.booking.app.controller;

import accommodation.booking.app.dto.location.LocationDto;
import accommodation.booking.app.service.LocationService;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RequiredArgsConstructor
@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/{id}")
    public LocationDto getLocation(@PathVariable Long id) {
        return locationService.getLocation(id);
    }

    @GetMapping()
    public List<LocationDto> getLocations() {
        return locationService.getLocations();
    }
}
