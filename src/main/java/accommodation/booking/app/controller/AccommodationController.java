package accommodation.booking.app.controller;

import accommodation.booking.app.dto.accommodation.AccommodationDto;
import accommodation.booking.app.dto.accommodation.AccommodationUpdateRequestDto;
import accommodation.booking.app.dto.accommodation.CreateAccommodationRequestDto;
import accommodation.booking.app.service.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Accommodation Controller", description = "Managing accommodation inventory")
@RequiredArgsConstructor
@RestController
@RequestMapping("/accommodations")
public class AccommodationController {

    private final AccommodationService accommodationService;

    @PostMapping()
    @Operation(summary = "Add new accommodation",
            description = "Allows to add new accommodation")
    @PreAuthorize("hasRole('ADMIN')")
    public AccommodationDto createAccommodation(
            @RequestBody @Valid CreateAccommodationRequestDto accommodationRequestDto) {
        return accommodationService.createAccommodation(accommodationRequestDto);
    }

    @GetMapping()
    @Operation(summary = "Get accommodations",
            description = "Provides a list of available accommodations")
    public List<AccommodationDto> getAccommodations() {
        return accommodationService.getAccommodations();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get accommodation by id",
            description = "Retrieves detailed information about a specific accommodation")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public AccommodationDto getAccommodationById(@PathVariable Long id) {
        return accommodationService.getAccommodation(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update accommodation",
            description = "Allows to update accommodation")
    @PreAuthorize("hasRole('ADMIN')")
    public AccommodationDto updateAccommodation(
            @PathVariable Long id, @RequestBody AccommodationUpdateRequestDto accommodationDto) {
        return accommodationService.updateAccommodation(id, accommodationDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete accommodation",
            description = "Allows to delete accommodation")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void deleteAccommodation(@PathVariable Long id) {
        accommodationService.deleteAccommodation(id);
    }
}
