package accommodation.booking.app.service.impl;

import static accommodation.booking.app.model.RoleName.CUSTOMER;

import accommodation.booking.app.dto.payment.CreatePaymentRequestDto;
import accommodation.booking.app.dto.payment.PaymentDto;
import accommodation.booking.app.dto.payment.PaymentResponseDto;
import accommodation.booking.app.exception.AccommodationException;
import accommodation.booking.app.exception.BookingException;
import accommodation.booking.app.exception.EntityNotFoundException;
import accommodation.booking.app.exception.PaymentException;
import accommodation.booking.app.mapper.PaymentMapper;
import accommodation.booking.app.model.Booking;
import accommodation.booking.app.model.Payment;
import accommodation.booking.app.model.Status;
import accommodation.booking.app.model.User;
import accommodation.booking.app.notification.telegram.NotificationService;
import accommodation.booking.app.repository.BookingRepository;
import accommodation.booking.app.repository.PaymentRepository;
import accommodation.booking.app.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final String CURRENCY = "USD";

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationService notifier;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${STRIPE_SECRET_KEY}")
    private String stripeSecretKey;

    @Override
    public List<PaymentDto> getAllPaymentsByUserId(Long id, User user) {
        if (id != user.getId() && user.getRole().equals(CUSTOMER)) {
            throw new BookingException("Logged user doesn't match with user id in path");
        }
        return paymentRepository.findAllByBookingId_User_Id(id).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public PaymentDto createPaymentSession(CreatePaymentRequestDto createPaymentRequestDto,
                                           User user) {
        Booking booking = getBookingById(createPaymentRequestDto.bookingId());
        validateBooking(booking);
        validateUser(booking.getUser().getId(), user);
        validateStripeSecretKey();
        BigDecimal amountToPay = amountToPay(booking);
        long amountInCents = toCents(amountToPay);
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(baseUrl + "/payments/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(baseUrl + "/payments/cancel?session_id={CHECKOUT_SESSION_ID}")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(CURRENCY)
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem
                                                                .PriceData.ProductData.builder()
                                                                .setName("Booking id: "
                                                                        + booking.getId())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("bookingId", String.valueOf(booking.getId()))
                .putMetadata("userId", String.valueOf(booking.getUser().getId()))
                .build();

        try {
            Session session = Session.create(params);

            Payment payment = new Payment()
                    .setBookingId(booking)
                    .setAmountToPay(amountToPay)
                    .setSessionId(session.getId())
                    .setSessionUrl(toUrl(session.getUrl()))
                    .setStatus(Status.valueOf("PENDING"));

            Payment savedPayment = paymentRepository.save(payment);
            return paymentMapper.toDto(savedPayment);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to create Stripe Checkout session", e);
        }
    }

    @Override
    @Transactional
    public PaymentResponseDto paymentSuccess(String sessionId) {
        validateStripeSecretKey();
        Payment payment = getPaymentBySessionId(sessionId);
        Booking booking = getBookingById(payment.getBookingId().getId());
        Stripe.apiKey = stripeSecretKey;

        try {
            Session session = Session.retrieve(sessionId);

            String paymentStatus = session.getPaymentStatus();
            if ("paid".equalsIgnoreCase(paymentStatus)) {
                payment.setStatus(Status.valueOf("CONFIRMED"));
                booking.setStatus(Status.valueOf("CONFIRMED"));
                paymentRepository.save(payment);
                bookingRepository.save(booking);
                PaymentResponseDto paymentResponseDto = paymentMapper.toResponseDto(payment);
                paymentResponseDto.setMessage("Payment completed");
                notifier.telegramSendMessage(paymentSucceededMessage(payment, booking));
                return paymentResponseDto;
            }
            return paymentMapper.toResponseDto(payment);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to verify Stripe session: " + sessionId, e);
        }
    }

    @Override
    @Transactional
    public PaymentResponseDto paymentCancel(String sessionId) {
        Payment payment = getPaymentBySessionId(sessionId);
        Booking booking = getBookingById(payment.getBookingId().getId());
        if (payment.getStatus() == Status.CONFIRMED) {
            PaymentResponseDto paymentResponseDto = paymentMapper.toResponseDto(payment);
            paymentResponseDto.setMessage("Payment already completed and cannot be canceled");
            return paymentResponseDto;
        }
        payment.setStatus(Status.valueOf("CANCELED"));
        booking.setStatus(Status.valueOf("CANCELED"));
        paymentRepository.save(payment);
        bookingRepository.save(booking);
        PaymentResponseDto paymentResponseDto = paymentMapper.toResponseDto(payment);
        paymentResponseDto.setMessage("Payment is canceled and can be made later, "
                + "but the session is available only for 24 hours");
        return paymentResponseDto;
    }

    private void validateBooking(Booking booking) {
        if (booking.getStatus() != Status.PENDING) {
            throw new PaymentException("Booking status is "
                    + booking.getStatus().toString().toLowerCase() + " and cannot paid for");
        }
        if (booking.isDeleted()) {
            throw new PaymentException("Booking is deleted and cannot be paid for");
        }
    }

    private void validateUser(Long userId, User user) {
        if (userId != user.getId()) {
            throw new BookingException("Logged user doesn't match with user id in booking");
        }
    }

    private Payment getPaymentBySessionId(String sessionId) {
        return paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment with id " + sessionId
                        + " not found"));
    }

    private void validateStripeSecretKey() {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalStateException("Stripe secret key is not configured");
        }
    }

    private URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Stripe session URL: " + url, e);
        }
    }

    private long toCents(BigDecimal amount) {
        return amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValueExact();
    }

    private BigDecimal amountToPay(Booking booking) {
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(),
                booking.getCheckOutDate());
        if (nights <= 0) {
            throw new AccommodationException("Checkout date must be after checkin date");
        }
        BigDecimal dailyRate = booking.getAccommodation().getDailyRate();
        return dailyRate.multiply(BigDecimal.valueOf(nights));
    }

    private Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Booking not found in database"));
    }

    private String paymentSucceededMessage(Payment payment, Booking booking) {
        Long bookingId = booking.getId();
        Long accommodationId =
                booking.getAccommodation() != null ? booking.getAccommodation().getId() : null;
        String userEmail = booking.getUser() != null ? booking.getUser().getEmail() : null;
        LocalDate checkInDate = booking.getCheckInDate();
        LocalDate checkOutDate = booking.getCheckOutDate();
        BigDecimal amountToPay = payment.getAmountToPay();
        Long paymentId = payment.getId();

        return """
                Payment has been made:
                - booking id: %s
                - accommodation id: %s
                - payment id: %s
                - user email: %s
                - check in: %s
                - check out: %s
                - amount has been payed : %s $
                """.formatted(bookingId, accommodationId, paymentId, userEmail, checkInDate,
                checkOutDate, amountToPay);
    }
}
