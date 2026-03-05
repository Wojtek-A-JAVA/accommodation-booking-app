package accommodation.booking.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import accommodation.booking.app.dto.accommodation.AccommodationDto;
import accommodation.booking.app.dto.accommodation.AccommodationUpdateRequestDto;
import accommodation.booking.app.dto.accommodation.CreateAccommodationRequestDto;
import accommodation.booking.app.exception.AccommodationException;
import accommodation.booking.app.exception.EntityNotFoundException;
import accommodation.booking.app.mapper.AccommodationMapper;
import accommodation.booking.app.model.Accommodation;
import accommodation.booking.app.model.Amenity;
import accommodation.booking.app.model.Location;
import accommodation.booking.app.model.Type;
import accommodation.booking.app.notification.telegram.NotificationService;
import accommodation.booking.app.repository.AccommodationRepository;
import accommodation.booking.app.repository.AmenityRepository;
import accommodation.booking.app.repository.LocationRepository;
import accommodation.booking.app.service.impl.AccommodationServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceTest {

    @InjectMocks
    private AccommodationServiceImpl service;

    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private AccommodationMapper accommodationMapper;
    @Mock
    private NotificationService notifier;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private AmenityRepository amenityRepository;

    @Test
    void createAccommodation_SendsTelegramNotificationAndReturnsDto() {
        CreateAccommodationRequestDto request = new CreateAccommodationRequestDto(
                Type.APARTMENT,
                1L,
                "Studio",
                List.of(1L, 3L),
                BigDecimal.valueOf(10),
                1
        );

        when(accommodationRepository.findByTypeAndSizeAndLocationId(Type.APARTMENT, "Studio", 1L))
                .thenReturn(Optional.empty());

        when(amenityRepository.findById(1L)).thenReturn(Optional.of(new Amenity().setId(1L)));
        when(amenityRepository.findById(3L)).thenReturn(Optional.of(new Amenity().setId(3L)));

        Location location = new Location().setId(1L).setCountry("Poland").setCity("Warsaw").setStreet("X");
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        Accommodation accommodation = new Accommodation()
                .setType(Type.APARTMENT)
                .setSize("Studio")
                .setDailyRate(BigDecimal.valueOf(10));

        when(accommodationMapper.toEntity(request)).thenReturn(accommodation);

        Accommodation savedAccommodation = new Accommodation()
                .setId(1L)
                .setType(Type.APARTMENT)
                .setSize("Studio")
                .setDailyRate(BigDecimal.valueOf(10))
                .setAvailability(1);

        when(accommodationRepository.save(accommodation)).thenReturn(savedAccommodation);

        AccommodationDto expectedDto = new AccommodationDto(
                1L, Type.APARTMENT, 1L, "Studio", List.of(1L, 3L), BigDecimal.valueOf(10), 1
        );
        when(accommodationMapper.toDto(savedAccommodation)).thenReturn(expectedDto);

        AccommodationDto actual = service.createAccommodation(request);

        assertNotNull(actual);
        assertEquals(1L, actual.id());
        assertEquals(Type.APARTMENT, actual.type());

        verify(accommodationRepository, times(1)).save(accommodation);
        verify(accommodationMapper, times(1)).toDto(savedAccommodation);
        verify(notifier, times(1)).telegramSendMessage(anyString());
    }

    @Test
    void createAccommodation_WhenAlreadyExists_ThrowsAccommodationException() {
        CreateAccommodationRequestDto request = new CreateAccommodationRequestDto(
                Type.APARTMENT,
                1L,
                "Studio",
                List.of(1L, 3L),
                BigDecimal.valueOf(10),
                1
        );

        when(accommodationRepository.findByTypeAndSizeAndLocationId(
                Type.APARTMENT, "Studio", 1L))
                .thenReturn(Optional.of(new Accommodation()));

        assertThrows(AccommodationException.class, () -> service.createAccommodation(request));

        verify(accommodationRepository).findByTypeAndSizeAndLocationId(
                Type.APARTMENT, "Studio", 1L);
        verifyNoInteractions(amenityRepository);
        verifyNoInteractions(locationRepository);
        verifyNoInteractions(accommodationMapper);
        verifyNoInteractions(notifier);
        verify(accommodationRepository, never()).save(any());
    }

    @Test
    void createAccommodation_WhenAmenityNotFound_ThrowsEntityNotFoundException() {
        CreateAccommodationRequestDto request = new CreateAccommodationRequestDto(
                Type.APARTMENT,
                1L,
                "Studio",
                List.of(999L),
                BigDecimal.valueOf(10),
                1
        );

        when(accommodationRepository.findByTypeAndSizeAndLocationId(Type.APARTMENT, "Studio", 1L))
                .thenReturn(Optional.empty());
        when(amenityRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> service.createAccommodation(request));
        assertTrue(exception.getMessage().contains("Amenity"));

        verify(amenityRepository).findById(999L);
        verifyNoInteractions(locationRepository);
        verifyNoInteractions(accommodationMapper);
        verifyNoInteractions(notifier);
        verify(accommodationRepository, never()).save(any());
    }

    @Test
    void getAccommodations_ReturnsAccommodationsDtos() {
        Accommodation apartmentAccommodation = new Accommodation()
                .setId(1L).setType(Type.APARTMENT).setSize("Studio");
        Accommodation houseAccommodation = new Accommodation()
                .setId(2L).setType(Type.HOUSE).setSize("150m2");
        when(accommodationRepository.findAll()).thenReturn(
                List.of(apartmentAccommodation, houseAccommodation));

        AccommodationDto apartmentAccommodationDto = new AccommodationDto(
                1L, Type.APARTMENT, 10L, "Studio", List.of(1L, 3L), BigDecimal.valueOf(10), 1);
        AccommodationDto houseAccommodationDto = new AccommodationDto(
                2L, Type.HOUSE, 11L, "200m2", List.of(2L, 5L), BigDecimal.valueOf(100), 1);
        when(accommodationMapper.toDto(apartmentAccommodation)).thenReturn(apartmentAccommodationDto);
        when(accommodationMapper.toDto(houseAccommodation)).thenReturn(houseAccommodationDto);

        List<AccommodationDto> actual = service.getAccommodations();

        assertNotNull(actual);
        assertEquals(2, actual.size());
        assertEquals(1L, actual.get(0).id());
        assertEquals(2L, actual.get(1).id());
        verify(accommodationRepository).findAll();
        verify(accommodationMapper).toDto(apartmentAccommodation);
        verify(accommodationMapper).toDto(houseAccommodation);
    }

    @Test
    void getAccommodation_ReturnsAccommodationDto() {
        Accommodation entity = new Accommodation().setId(1L).setType(Type.APARTMENT).setSize("Studio");
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(entity));

        AccommodationDto dto = new AccommodationDto(
                1L, Type.APARTMENT, 10L, "Studio", List.of(1L, 3L), BigDecimal.valueOf(10), 1);
        when(accommodationMapper.toDto(entity)).thenReturn(dto);

        AccommodationDto actual = service.getAccommodation(1L);

        assertNotNull(actual);
        assertEquals(1L, actual.id());
        verify(accommodationRepository).findById(1L);
        verify(accommodationMapper).toDto(entity);
    }

    @Test
    void updateAccommodation_ReturnsAccommodationDto() {
        Accommodation entity = new Accommodation()
                .setId(1L).setType(Type.APARTMENT).setSize("Studio")
                .setAmenities(new ArrayList<>());

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(entity));

        Amenity amenity2 = new Amenity().setId(2L);
        Amenity amenity3 = new Amenity().setId(3L);
        when(accommodationMapper.mapIdToAmenities(2L)).thenReturn(amenity2);
        when(accommodationMapper.mapIdToAmenities(3L)).thenReturn(amenity3);

        AccommodationUpdateRequestDto update = new AccommodationUpdateRequestDto(
                List.of(2L, 3L),
                BigDecimal.valueOf(100),
                1
        );

        AccommodationDto dto = new AccommodationDto(
                1L, Type.APARTMENT, 1L, "Studio", List.of(2L, 3L), BigDecimal.valueOf(100), 1
        );
        when(accommodationMapper.toDto(entity)).thenReturn(dto);

        AccommodationDto actual = service.updateAccommodation(1L, update);

        assertNotNull(actual);
        assertEquals(1L, actual.id());
        verify(accommodationRepository).findById(1L);
        verify(accommodationMapper).mapIdToAmenities(2L);
        verify(accommodationMapper).mapIdToAmenities(3L);
        verify(accommodationRepository).save(entity);
        verify(accommodationMapper).toDto(entity);
    }

    @Test
    void deleteAccommodation_DeletesById() {
        when(accommodationRepository.findById(2L)).thenReturn(Optional.of(new Accommodation().setId(2L)));

        service.deleteAccommodation(2L);

        verify(accommodationRepository).findById(2L);
        verify(accommodationRepository).deleteById(2L);
    }
}
