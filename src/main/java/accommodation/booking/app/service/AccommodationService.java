package accommodation.booking.app.service;

import accommodation.booking.app.dto.accommodation.AccommodationDto;
import accommodation.booking.app.dto.accommodation.AccommodationUpdateRequestDto;
import accommodation.booking.app.dto.accommodation.CreateAccommodationRequestDto;
import java.time.LocalDate;
import java.util.List;

public interface AccommodationService {

    AccommodationDto createAccommodation(CreateAccommodationRequestDto accommodationRequestDto);

    List<AccommodationDto> getAccommodations();

    AccommodationDto getAccommodation(Long id);

    List<AccommodationDto> searchAccommodation(String city, LocalDate checkIn, LocalDate checkOut);

    AccommodationDto updateAccommodation(Long id, AccommodationUpdateRequestDto accommodationDto);

    void deleteAccommodation(Long id);
}
