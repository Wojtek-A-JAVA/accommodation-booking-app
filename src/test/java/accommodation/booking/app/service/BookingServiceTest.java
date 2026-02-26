package accommodation.booking.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import accommodation.booking.app.dto.booking.BookingDto;
import accommodation.booking.app.dto.booking.BookingUpdateRequestDto;
import accommodation.booking.app.dto.booking.CreateBookingRequestDto;
import accommodation.booking.app.exception.BookingException;
import accommodation.booking.app.exception.EntityNotFoundException;
import accommodation.booking.app.mapper.BookingMapper;
import accommodation.booking.app.model.Accommodation;
import accommodation.booking.app.model.Booking;
import accommodation.booking.app.model.Location;
import accommodation.booking.app.model.Payment;
import accommodation.booking.app.model.Role;
import accommodation.booking.app.model.RoleName;
import accommodation.booking.app.model.Status;
import accommodation.booking.app.model.User;
import accommodation.booking.app.notification.telegram.NotificationService;
import accommodation.booking.app.repository.AccommodationRepository;
import accommodation.booking.app.repository.BookingRepository;
import accommodation.booking.app.repository.PaymentRepository;
import accommodation.booking.app.repository.UserRepository;
import accommodation.booking.app.service.impl.BookingServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    private BookingServiceImpl service;

    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notifier;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @Test
    void createBooking_SavesBookingAndReturnsDto() {
        Role customerRole = new Role().setRoleName(RoleName.CUSTOMER);
        User user = new User().setId(3L).setEmail("james@google.com").setRole(customerRole);

        when(userRepository.findByEmail("james@google.com")).thenReturn(Optional.of(user));
        when(paymentRepository.findAllByBookingId_User_IdAndStatus(3L, Status.PENDING))
                .thenReturn(List.of());

        Location location = new Location().setId(7L);
        Accommodation accommodation = new Accommodation()
                .setId(5L)
                .setLocation(location)
                .setDailyRate(BigDecimal.valueOf(25));

        when(accommodationRepository.findById(5L)).thenReturn(Optional.of(accommodation));

        CreateBookingRequestDto request = new CreateBookingRequestDto(
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                5L
        );

        Booking booking = new Booking()
                .setId(99L)
                .setAccommodation(new Accommodation().setId(5L))
                .setCheckInDate(request.checkInDate())
                .setCheckOutDate(request.checkOutDate());

        when(bookingMapper.toEntity(request)).thenReturn(booking);

        when(bookingRepository.findReservedAccommodations(
                eq(5L),
                eq(request.checkInDate()),
                eq(EnumSet.of(Status.CANCELED, Status.EXPIRED))
        )).thenReturn(List.of());

        BookingDto bookingDto = new BookingDto(
                99L,
                request.checkInDate(),
                request.checkOutDate(),
                5L,
                10L,
                "PENDING"
        );
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        BookingDto actual = service.createBooking(request, "james@google.com");

        assertNotNull(actual);
        assertEquals(99L, actual.id());
        assertEquals("PENDING", actual.status());

        verify(bookingRepository).save(booking);
        verify(bookingMapper).toDto(booking);
    }

    @Test
    void createBooking_WhenCustomerHasPendingPayment_ThrowsBookingException() {
        Role customerRole = new Role().setRoleName(RoleName.CUSTOMER);

        User user = new User().setId(3L).setEmail("james@google.com").setRole(customerRole);
        when(userRepository.findByEmail("james@google.com")).thenReturn(Optional.of(user));

        when(paymentRepository.findAllByBookingId_User_IdAndStatus(3L, Status.PENDING))
                .thenReturn(List.of(new Payment().setId(1L)));

        CreateBookingRequestDto request = new CreateBookingRequestDto(
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(11),
                1L
        );

        assertThrows(BookingException.class,
                () -> service.createBooking(request, "james@google.com"));

        verifyNoInteractions(accommodationRepository);
        verify(bookingRepository, never()).save(any());
        verify(notifier, never()).telegramSendMessage(anyString());
    }

    @Test
    void createBooking_WhenDatesAlreadyReserved_ThrowsBookingException() {
        Role customerRole = new Role().setRoleName(RoleName.CUSTOMER);
        User user = new User().setId(3L).setEmail("james@google.com").setRole(customerRole);

        when(userRepository.findByEmail("james@google.com")).thenReturn(Optional.of(user));
        when(paymentRepository.findAllByBookingId_User_IdAndStatus(3L, Status.PENDING))
                .thenReturn(List.of());

        Location location = new Location().setId(7L);
        Accommodation accommodation = new Accommodation()
                .setId(5L)
                .setLocation(location)
                .setDailyRate(BigDecimal.valueOf(25));
        when(accommodationRepository.findById(5L)).thenReturn(Optional.of(accommodation));

        CreateBookingRequestDto requestDto = new CreateBookingRequestDto(
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                5L
        );

        Booking booking = new Booking()
                .setAccommodation(new Accommodation().setId(5L))
                .setCheckInDate(requestDto.checkInDate())
                .setCheckOutDate(requestDto.checkOutDate());

        when(bookingMapper.toEntity(requestDto)).thenReturn(booking);

        when(bookingRepository.findReservedAccommodations(
                eq(5L),
                eq(requestDto.checkInDate()),
                eq(EnumSet.of(Status.CANCELED, Status.EXPIRED))
        )).thenReturn(List.of(new Booking().setId(1L)));

        assertThrows(BookingException.class,
                () -> service.createBooking(requestDto, "james@google.com"));

        verify(bookingRepository, never()).save(any());
        verify(notifier, never()).telegramSendMessage(anyString());
    }

    @Test
    void getBookingsByUserIdAndStatus_WhenFound_ReturnsMappedDtos() {
        Booking booking1 = new Booking().setId(1L);
        Booking booking2 = new Booking().setId(2L);
        when(bookingRepository.findByUserIdAndStatus(2L, Status.CONFIRMED))
                .thenReturn(List.of(booking1, booking2));

        BookingDto bookingDto1 = new BookingDto(1L, LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2), 5L, 2L, "CONFIRMED");
        BookingDto bookingDto2 = new BookingDto(2L, LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(4), 6L, 2L, "CONFIRMED");
        when(bookingMapper.toDto(booking1)).thenReturn(bookingDto1);
        when(bookingMapper.toDto(booking2)).thenReturn(bookingDto2);

        List<BookingDto> actual = service.getBookingsByUserIdAndStatus(2L, "CONFIRMED");

        assertEquals(2, actual.size());
        assertEquals(1L, actual.get(0).id());
        assertEquals(2L, actual.get(1).id());
        verify(bookingMapper).toDto(booking1);
        verify(bookingMapper).toDto(booking2);
    }

    @Test
    void getUserBookings_ReturnsMappedDtos() {
        Role customerRole = new Role().setRoleName(RoleName.CUSTOMER);
        User user = new User().setId(3L).setEmail("james@google.com").setRole(customerRole);

        when(userRepository.findByEmail("james@google.com")).thenReturn(Optional.of(user));

        Booking booking = new Booking().setId(1L);
        when(bookingRepository.findByUserId(3L)).thenReturn(List.of(booking));

        BookingDto bookingDto = new BookingDto(1L, LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2), 5L, 4L, "PENDING");
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        List<BookingDto> actual = service.getUserBookings("james@google.com");

        assertEquals(1, actual.size());
        assertEquals(1L, actual.get(0).id());
        verify(bookingMapper).toDto(booking);
    }

    @Test
    void getBookingById_WhenFound_ReturnsMappedDto() {
        Booking booking = new Booking().setId(8L);
        when(bookingRepository.findById(8L)).thenReturn(Optional.of(booking));

        BookingDto bookingDto = new BookingDto(8L, LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2), 5L, 4L, "PENDING");
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        BookingDto actual = service.getBookingById(8L);

        assertNotNull(actual);
        assertEquals(8L, actual.id());
        verify(bookingMapper).toDto(booking);
    }

    @Test
    void getBookingById_WhenNotFound_ThrowsEntityNotFoundException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getBookingById(999L));

        verifyNoInteractions(bookingMapper);
    }

    @Test
    void updateBooking_WhenBookingNotFound_ThrowsEntityNotFoundException() {
        Role customerRole = new Role().setRoleName(RoleName.CUSTOMER);
        User user = new User().setId(3L).setEmail("james@google.com").setRole(customerRole);

        when(userRepository.findByEmail("james@google.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        BookingUpdateRequestDto update = new BookingUpdateRequestDto(
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                "CONFIRMED"
        );

        assertThrows(EntityNotFoundException.class,
                () -> service.updateBooking(999L, update, "james@google.com"));

        verify(bookingRepository, never()).save(any());
        verify(notifier, never()).telegramSendMessage(anyString());
    }

    @Test
    void updateBooking_SavesSendsTelegramNotificationAndReturnsDto() {
        Role customerRole = new Role().setRoleName(RoleName.CUSTOMER);
        User user = new User().setId(3L).setEmail("james@google.com").setRole(customerRole);

        when(userRepository.findByEmail("james@google.com")).thenReturn(Optional.of(user));

        Booking booking = new Booking()
                .setId(1L)
                .setUser(user)
                .setAccommodation(new Accommodation().setId(5L))
                .setStatus(Status.PENDING)
                .setCheckInDate(LocalDate.now().plusDays(10))
                .setCheckOutDate(LocalDate.now().plusDays(12));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        when(bookingRepository.checkForActiveBookingsOnDate(eq(5L), any(LocalDate.class),
                eq(EnumSet.of(Status.PENDING, Status.CONFIRMED))))
                .thenReturn(false);

        BookingUpdateRequestDto update = new BookingUpdateRequestDto(
                LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(25),
                "PENDING"
        );

        BookingDto bookingDto = new BookingDto(
                1L, update.checkInDate(), update.checkOutDate(), 5L, 4L, "PENDING"
        );
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        BookingDto actual = service.updateBooking(1L, update, "james@google.com");

        assertNotNull(actual);
        assertEquals("PENDING", actual.status());
        verify(bookingRepository).save(booking);
        verify(bookingMapper).toDto(booking);
    }

    @Test
    void deleteBooking_Deletes() {
        Booking booking = new Booking()
                .setId(9L)
                .setAccommodation(new Accommodation().setId(77L))
                .setUser(new User().setId(1L));

        when(bookingRepository.findById(9L)).thenReturn(Optional.of(booking));

        service.deleteBooking(9L);

        verify(bookingRepository).delete(booking);
    }

    @Test
    void expireOldBookings_ExpiresEachBooking() {
        LocalDate today = LocalDate.now();
        Booking booking1 = new Booking()
                .setId(1L)
                .setAccommodation(new Accommodation().setId(10L))
                .setUser(new User().setEmail("test1@test.com"))
                .setCheckInDate(today.minusDays(2))
                .setCheckOutDate(today.minusDays(1))
                .setStatus(Status.PENDING);
        Booking booking2 = new Booking()
                .setId(2L)
                .setAccommodation(new Accommodation().setId(11L))
                .setUser(new User().setEmail("test2@test.com"))
                .setCheckInDate(today.minusDays(5))
                .setCheckOutDate(today.minusDays(1))
                .setStatus(Status.CONFIRMED);

        when(bookingRepository.findBookingsToExpire(today,
                EnumSet.of(Status.PENDING, Status.CONFIRMED)))
                .thenReturn(List.of(booking1, booking2));

        service.expireOldBookings(today);

        assertEquals(Status.EXPIRED, booking1.getStatus());
        assertEquals(Status.EXPIRED, booking2.getStatus());
        verify(bookingRepository).save(booking1);
        verify(bookingRepository).save(booking2);
    }
}
