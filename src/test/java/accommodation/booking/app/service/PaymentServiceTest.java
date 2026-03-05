package accommodation.booking.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import accommodation.booking.app.dto.payment.CreatePaymentRequestDto;
import accommodation.booking.app.dto.payment.PaymentDto;
import accommodation.booking.app.dto.payment.PaymentResponseDto;
import accommodation.booking.app.mapper.PaymentMapper;
import accommodation.booking.app.model.Accommodation;
import accommodation.booking.app.model.Booking;
import accommodation.booking.app.model.Payment;
import accommodation.booking.app.model.Role;
import accommodation.booking.app.model.RoleName;
import accommodation.booking.app.model.Status;
import accommodation.booking.app.model.User;
import accommodation.booking.app.notification.telegram.NotificationService;
import accommodation.booking.app.repository.BookingRepository;
import accommodation.booking.app.repository.PaymentRepository;
import accommodation.booking.app.repository.UserRepository;
import accommodation.booking.app.service.impl.PaymentServiceImpl;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentServiceImpl service;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private NotificationService notifier;

    @Test
    void getAllPaymentsByUserId_ReturnsDtos() {
        Role role = new Role().setRoleName(RoleName.CUSTOMER);
        User user = new User().setId(3L).setEmail("james@google.com").setRole(role);

        when(userRepository.findByEmail("james@google.com")).thenReturn(Optional.of(user));

        Booking booking = new Booking().setId(1L);
        Payment payment = new Payment().setId(10L).setBookingId(booking).setStatus(Status.PENDING);

        when(paymentRepository.findAllByBookingId_User_Id(3L)).thenReturn(List.of(payment));

        PaymentDto dto = new PaymentDto(10L, 1L, "PENDING", BigDecimal.valueOf(10),
                "cs_1", "http://test");
        when(paymentMapper.toDto(payment)).thenReturn(dto);

        List<PaymentDto> actual = service.getAllPaymentsByUserId(3L, "james@google.com");

        assertEquals(1, actual.size());
        assertEquals(10L, actual.getFirst().id());
        verify(paymentRepository).findAllByBookingId_User_Id(3L);
        verify(paymentMapper).toDto(payment);
    }

    @Test
    void createPaymentSession_SessionCreate() {
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "stripeSecretKey", "sk_test");
        Role customerRole = new Role().setRoleName(RoleName.CUSTOMER);
        User user = new User().setId(3L).setEmail("james@google.com").setRole(customerRole);

        when(userRepository.findByEmail("james@google.com")).thenReturn(Optional.of(user));

        Accommodation accommodation = new Accommodation().setDailyRate(BigDecimal.valueOf(20)).setId(1L);
        Booking booking = new Booking()
                .setId(1L)
                .setUser(user)
                .setAccommodation(accommodation)
                .setCheckInDate(LocalDate.now().plusDays(1))
                .setCheckOutDate(LocalDate.now().plusDays(3))
                .setStatus(Status.PENDING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Payment savedPayment = new Payment()
                .setId(50L)
                .setBookingId(booking)
                .setStatus(Status.PENDING)
                .setAmountToPay(BigDecimal.valueOf(40))
                .setSessionId("cs_test_123");

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentDto mapped = new PaymentDto(
                50L, 1L, "PENDING", BigDecimal.valueOf(40), "cs_test_123", "http://test.com"
        );
        when(paymentMapper.toDto(savedPayment)).thenReturn(mapped);

        Session stripeSession = mock(Session.class);
        when(stripeSession.getId()).thenReturn("cs_test_123");
        when(stripeSession.getUrl()).thenReturn("http://test.com");

        try (MockedStatic<Session> mocked = mockStatic(Session.class)) {
            mocked.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(stripeSession);

            PaymentDto actual = service.createPaymentSession(
                    new CreatePaymentRequestDto(1L),
                    "james@google.com"
            );

            assertNotNull(actual);
            assertEquals(50L, actual.id());
            assertEquals("PENDING", actual.status());
            assertEquals(1L, actual.bookingId());
        }
    }

    @Test
    void createPaymentSession_WhenStripeSecretKeyIsMissing_ThrowsException() {
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "stripeSecretKey", "");
        Role role = new Role().setRoleName(RoleName.CUSTOMER);
        User user = new User().setId(3L).setEmail("james@google.com").setRole(role);

        when(userRepository.findByEmail("james@google.com")).thenReturn(Optional.of(user));

        Accommodation accommodation = new Accommodation().setDailyRate(BigDecimal.valueOf(20)).setId(1L);
        Booking booking = new Booking()
                .setId(1L)
                .setUser(user)
                .setAccommodation(accommodation)
                .setCheckInDate(LocalDate.now().plusDays(1))
                .setCheckOutDate(LocalDate.now().plusDays(3))
                .setStatus(Status.PENDING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
                () -> service.createPaymentSession(new CreatePaymentRequestDto(1L), "james@google.com"));

        verify(paymentRepository, never()).save(any());
    }


    @Test
    void paymentSuccess_WhenStripeSaysPaid() {
        ReflectionTestUtils.setField(service, "stripeSecretKey", "sk_test");
        Booking booking = new Booking().setId(1L).setStatus(Status.PENDING);
        Payment payment = new Payment().setId(10L).setSessionId("test_id1").setStatus(Status.PENDING).setBookingId(booking);

        when(paymentRepository.findBySessionId("test_id1")).thenReturn(Optional.of(payment));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        accommodation.booking.app.dto.payment.PaymentResponseDto responseDto =
                new accommodation.booking.app.dto.payment.PaymentResponseDto();
        responseDto.setPaymentId(10L);
        responseDto.setStatus("CONFIRMED");

        when(paymentMapper.toResponseDto(payment)).thenReturn(responseDto);

        Session stripeSession = mock(Session.class);

        when(stripeSession.getPaymentStatus()).thenReturn("paid");

        try (MockedStatic<Session> mocked = mockStatic(Session.class)) {
            mocked.when(() -> Session.retrieve("test_id1")).thenReturn(stripeSession);

            PaymentResponseDto actual = service.paymentSuccess("test_id1");

            assertEquals(10L, actual.getPaymentId());
            assertEquals("Payment completed", actual.getMessage());
            verify(paymentRepository).save(payment);
            verify(bookingRepository).save(booking);
            verify(notifier).telegramSendMessage(anyString());
        }
    }

    @Test
    void paymentCancel_CancelsPaymentAndBooking() {
        Booking booking = new Booking().setId(1L).setStatus(Status.PENDING);
        Payment payment = new Payment()
                .setId(10L)
                .setSessionId("sess_2")
                .setStatus(Status.PENDING)
                .setBookingId(booking);

        when(paymentRepository.findBySessionId("sess_2")).thenReturn(Optional.of(payment));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        accommodation.booking.app.dto.payment.PaymentResponseDto responseDto =
                new accommodation.booking.app.dto.payment.PaymentResponseDto();
        responseDto.setPaymentId(10L);
        responseDto.setStatus("CANCELED");

        when(paymentMapper.toResponseDto(payment)).thenReturn(responseDto);

        PaymentResponseDto actual = service.paymentCancel("sess_2");

        assertEquals(10L, actual.getPaymentId());
        assertEquals("CANCELED", actual.getStatus());

        assertEquals(Status.CANCELED, payment.getStatus());
        assertEquals(Status.CANCELED, booking.getStatus());

        verify(paymentRepository).save(payment);
        verify(bookingRepository).save(booking);
        assertNotNull(actual.getMessage());
    }
}
