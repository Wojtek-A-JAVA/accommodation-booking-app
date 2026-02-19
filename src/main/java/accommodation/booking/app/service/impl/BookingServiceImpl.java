package accommodation.booking.app.service.impl;

import static accommodation.booking.app.model.RoleName.CUSTOMER;

import accommodation.booking.app.dto.booking.BookingDto;
import accommodation.booking.app.dto.booking.BookingUpdateRequestDto;
import accommodation.booking.app.dto.booking.CreateBookingRequestDto;
import accommodation.booking.app.exception.BookingException;
import accommodation.booking.app.exception.EntityNotFoundException;
import accommodation.booking.app.mapper.BookingMapper;
import accommodation.booking.app.model.Accommodation;
import accommodation.booking.app.model.Booking;
import accommodation.booking.app.model.Payment;
import accommodation.booking.app.model.Status;
import accommodation.booking.app.model.User;
import accommodation.booking.app.notification.telegram.NotificationService;
import accommodation.booking.app.repository.AccommodationRepository;
import accommodation.booking.app.repository.BookingRepository;
import accommodation.booking.app.repository.PaymentRepository;
import accommodation.booking.app.service.BookingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final NotificationService notifier;
    private final NotificationService notificationService;
    private final AccommodationRepository accommodationRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public BookingDto createBooking(CreateBookingRequestDto bookingRequestDtoDto, User user) {
        checkUserPayments(user);
        final Accommodation accommodation = accommodationRepository.findById(
                bookingRequestDtoDto.accommodationId()).orElseThrow(
                    () -> new EntityNotFoundException("Accommodation not found in database"));
        Booking booking = bookingMapper.toEntity(bookingRequestDtoDto);
        List<Booking> existingBookings = bookingRepository
                .findReservedAccommodations(booking.getAccommodation().getId(),
                        booking.getCheckInDate(), EnumSet.of(Status.CANCELED, Status.EXPIRED));
        if (!existingBookings.isEmpty()) {
            throw new BookingException("Accommodation is already booked at the given dates");
        }
        booking.setUser(user);
        booking.setStatus(Status.PENDING);
        bookingRepository.save(booking);
        notifier.telegramSendMessage(createdBookingMessage(booking, accommodation));
        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByUserIdAndStatus(Long id, String status) {
        List<Booking> bookingList =
                bookingRepository.findByUserIdAndStatus(id, Status.valueOf(status.toUpperCase()));
        if (bookingList.isEmpty()) {
            return List.of();
        } else {
            return bookingList.stream()
                    .map(bookingMapper::toDto)
                    .toList();
        }
    }

    @Override
    public List<BookingDto> getUserBookings(User user) {
        List<Booking> bookingList = bookingRepository.findByUserId(user.getId());
        if (bookingList.isEmpty()) {
            return List.of();
        } else {
            return bookingList.stream()
                    .map(bookingMapper::toDto)
                    .toList();
        }
    }

    @Override
    public BookingDto getBookingById(Long id) {
        Booking booking = findBookingInDb(id);
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public BookingDto updateBooking(Long id, BookingUpdateRequestDto bookingUpdateRequestDto,
                                    User user) {
        Booking booking = findBookingInDb(id);
        if (!booking.getUser().equals(user) && user.getRole().equals(CUSTOMER)) {
            throw new BookingException("Logged customer doesn't match with booking user "
                    + "or user is not admin");
        }
        if (booking.getStatus().equals(Status.CANCELED)) {
            throw new BookingException("Booking is already canceled");
        }
        String status = "is still: " + booking.getStatus().toString();
        checkBookingDates(bookingUpdateRequestDto, booking);
        if (bookingUpdateRequestDto.status() != null) {
            booking.setStatus(Status.valueOf(bookingUpdateRequestDto.status().toUpperCase()));
            status = "has been changed to: " + bookingUpdateRequestDto.status();
        }
        String checkInDate = "is still: " + booking.getCheckInDate();
        if (bookingUpdateRequestDto.checkInDate() != null) {
            booking.setCheckInDate(bookingUpdateRequestDto.checkInDate());
            checkInDate = "has been changed to: " + bookingUpdateRequestDto.checkInDate();

        }
        String checkOutDate = "is still: " + booking.getCheckOutDate();
        if (bookingUpdateRequestDto.checkOutDate() != null) {
            booking.setCheckOutDate(bookingUpdateRequestDto.checkOutDate());
            checkOutDate = "has been changed to: " + bookingUpdateRequestDto.checkOutDate();
        }
        bookingRepository.save(booking);
        notifier.telegramSendMessage(updateBookingMessage(booking, status, checkInDate,
                checkOutDate));
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = findBookingInDb(id);
        bookingRepository.delete(booking);
        notifier.telegramSendMessage("Booking with id: " + booking.getId()
                + " for accommodation with id " + booking.getAccommodation().getId()
                + " was deleted");
    }

    @Override
    @Transactional
    public void expireOldBookings(LocalDate today) {
        List<Booking> expiredBookings = bookingRepository
                .findBookingsToExpire(today, EnumSet.of(Status.PENDING, Status.CONFIRMED));

        if (expiredBookings.isEmpty()) {
            notificationService.telegramSendMessage("No expired bookings today!");
            return;
        }

        for (Booking booking : expiredBookings) {
            String message = expiredBookingMessage(booking);
            notificationService.telegramSendMessage(message);
            booking.setStatus(Status.EXPIRED);
            bookingRepository.save(booking);
        }
    }

    private void checkUserPayments(User user) {
        if (user.getRole().getRoleName().equals(CUSTOMER)) {
            List<Payment> unpaidPaymentsList = paymentRepository
                    .findAllByBookingId_User_IdAndStatus(user.getId(), Status.PENDING);
            int nr = unpaidPaymentsList.size();
            if (nr == 1) {
                throw new BookingException("You have 1 unpaid payment");
            }
            if (nr > 1) {
                throw new BookingException("You have " + nr + " unpaid payments");
            }
        }
    }

    private Booking findBookingInDb(Long id) {
        return bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Booking not found in database"));
    }

    private void checkBookingDates(BookingUpdateRequestDto bookingUpdateRequestDto,
                                   Booking booking) {
        Long accommodationId = booking.getAccommodation().getId();
        Set<Status> statuses = EnumSet.of(Status.PENDING, Status.CONFIRMED);

        if (bookingUpdateRequestDto.checkInDate() != null
                && bookingUpdateRequestDto.checkOutDate() != null
                && bookingUpdateRequestDto.checkInDate()
                .isAfter(bookingUpdateRequestDto.checkOutDate())) {
            throw new BookingException("Check in date cannot be after check out date");
        }
        if (bookingUpdateRequestDto.checkInDate() != null) {
            if (bookingUpdateRequestDto.checkInDate().isBefore(LocalDate.now())) {
                throw new BookingException("Date cannot be in the past");
            }
            if (bookingRepository.checkForActiveBookingsOnDate(
                    accommodationId, bookingUpdateRequestDto.checkInDate(), statuses)) {
                throw new BookingException("Accommodation is booked at the given date");
            }
        }
        if (bookingUpdateRequestDto.checkOutDate() != null) {
            if (bookingUpdateRequestDto.checkOutDate().isBefore(LocalDate.now())) {
                throw new BookingException("Date cannot be in the past");
            }
            if (bookingRepository.checkForActiveBookingsOnDate(
                    accommodationId, bookingUpdateRequestDto.checkOutDate(), statuses)) {
                throw new BookingException("Accommodation is booked at the given date");
            }
        }
    }

    private String createdBookingMessage(Booking booking, Accommodation accommodation) {
        Long bookingId = booking.getId();
        Long accommodationId =
                booking.getAccommodation() != null ? booking.getAccommodation().getId() : null;
        String userEmail = booking.getUser() != null ? booking.getUser().getEmail() : null;
        LocalDate checkInDate = booking.getCheckInDate();
        LocalDate checkOutDate = booking.getCheckOutDate();
        String status = booking.getStatus().toString();
        Long locationId = accommodation.getLocation().getId();
        BigDecimal dailyRate = accommodation.getDailyRate();

        return """
                New booking created:
                - booking id: %s
                - accommodation id: %s
                - user email: %s
                - check in: %s
                - check out: %s
                - status: %s
                - location id: %s
                - daily rate: %s
                """.formatted(bookingId, accommodationId, userEmail, checkInDate, checkOutDate,
                status, locationId, dailyRate);
    }

    private String expiredBookingMessage(Booking booking) {
        Long bookingId = booking.getId();
        Long accommodationId =
                booking.getAccommodation() != null ? booking.getAccommodation().getId() : null;
        String userEmail = booking.getUser() != null ? booking.getUser().getEmail() : null;
        LocalDate checkInDate = booking.getCheckInDate();
        LocalDate checkOutDate = booking.getCheckOutDate();

        return """
                Booking expired and accommodation released
                - booking id: %s
                - accommodation id: %s
                - user email: %s
                - check in: %s
                - check out: %s
                """.formatted(bookingId, accommodationId, userEmail, checkInDate, checkOutDate);
    }

    private String updateBookingMessage(Booking booking, String status, String checkInDate,
                                        String checkOutDate) {
        return """
                Booking updated:
                - booking id: %s
                - status %s
                - check in %s
                - check out %s
                """.formatted(booking.getId(), status, checkInDate, checkOutDate);
    }
}

