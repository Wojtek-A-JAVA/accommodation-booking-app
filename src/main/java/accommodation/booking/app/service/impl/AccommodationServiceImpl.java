package accommodation.booking.app.service.impl;

import accommodation.booking.app.dto.accommodation.AccommodationDto;
import accommodation.booking.app.dto.accommodation.AccommodationUpdateRequestDto;
import accommodation.booking.app.dto.accommodation.CreateAccommodationRequestDto;
import accommodation.booking.app.exception.AccommodationException;
import accommodation.booking.app.exception.EntityNotFoundException;
import accommodation.booking.app.mapper.AccommodationMapper;
import accommodation.booking.app.model.Accommodation;
import accommodation.booking.app.model.Location;
import accommodation.booking.app.notification.telegram.NotificationService;
import accommodation.booking.app.repository.AccommodationRepository;
import accommodation.booking.app.repository.AmenityRepository;
import accommodation.booking.app.repository.LocationRepository;
import accommodation.booking.app.service.AccommodationService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final NotificationService notifier;
    private final LocationRepository locationRepository;
    private final AmenityRepository amenityRepository;

    @Override
    @Transactional
    public AccommodationDto createAccommodation(
            CreateAccommodationRequestDto accommodationRequestDto) {
        if (accommodationRepository.findByTypeAndSizeAndLocationId(
                accommodationRequestDto.type(),
                accommodationRequestDto.size(),
                accommodationRequestDto.locationId()).isPresent()) {
            throw new AccommodationException("Accommodation already exists in database");
        }
        checkAmenities(accommodationRequestDto.amenityIds());
        Location location = locationRepository.findById(
                accommodationRequestDto.locationId()).orElseThrow(
                    () -> new EntityNotFoundException("Location not found in database")
        );
        Accommodation accommodation = accommodationMapper.toEntity(accommodationRequestDto);
        notifier.telegramSendMessage(createAccommodationMessage(accommodation, location));
        return accommodationMapper.toDto(accommodationMapper.toEntity(accommodationRequestDto));
    }

    @Override
    public List<AccommodationDto> getAccommodations() {
        List<Accommodation> accommodationList = accommodationRepository.findAll();
        return accommodationList.stream()
                .map(accommodationMapper::toDto)
                .toList();
    }

    @Override
    public AccommodationDto getAccommodation(Long id) {
        Accommodation accommodation = getAccommodationById(id);
        return accommodationMapper.toDto(accommodation);
    }

    @Override
    @Transactional
    public AccommodationDto updateAccommodation(Long id,
                                                AccommodationUpdateRequestDto accommodationDto) {
        Accommodation accommodation = getAccommodationById(id);
        if (accommodationDto.amenityIds() != null) {
            accommodation.getAmenities().clear();
            accommodation.getAmenities().addAll(
                    accommodationDto.amenityIds().stream()
                            .map(accommodationMapper::mapIdToAmenities)
                            .toList());
        }
        if (accommodationDto.dailyRate() != null) {
            accommodation.setDailyRate(accommodationDto.dailyRate());
        }
        if (accommodationDto.availability() != null) {
            accommodation.setAvailability(accommodationDto.availability());
        }
        accommodationRepository.save(accommodation);
        return accommodationMapper.toDto(accommodation);
    }

    @Override
    @Transactional
    public void deleteAccommodation(Long id) {
        Accommodation accommodation = getAccommodationById(id);
        accommodationRepository.deleteById(id);
    }

    private Accommodation getAccommodationById(Long id) {
        return accommodationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Accommodation not found in database"));
    }

    private void checkAmenities(List<Long> amenityIds) {
        for (Long id : amenityIds) {
            amenityRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Amenity "
                    + id + " not found in database"));
        }
    }

    private String createAccommodationMessage(Accommodation accommodation, Location location) {
        LocalDate today = LocalDate.now();

        return """
                 New accommodation created:
                 - type: %s
                 - size: %s
                 - location:\s
                   Country - %s, City - %s, Street - %s
                 - daily rate: %s
                 - date: %s
                """.formatted(
                accommodation.getType(),
                accommodation.getSize(),
                location.getCountry(),
                location.getCity(),
                location.getStreet(),
                accommodation.getDailyRate(),
                today
        );
    }
}
