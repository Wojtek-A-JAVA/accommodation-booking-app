package accommodation.booking.app.service;

import accommodation.booking.app.dto.booking.BookingDto;
import accommodation.booking.app.dto.booking.BookingUpdateRequestDto;
import accommodation.booking.app.dto.booking.CreateBookingRequestDto;
import accommodation.booking.app.model.User;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingDto createBooking(@Valid CreateBookingRequestDto bookingRequestDtoDto, User user);

    List<BookingDto> getBookingsByUserIdAndStatus(Long id, String status);

    List<BookingDto> getUserBookings(User user);

    BookingDto getBookingById(Long id);

    BookingDto updateBooking(Long id, BookingUpdateRequestDto bookingUpdateRequestDto, User user);

    void deleteBooking(Long id);

    void expireOldBookings(LocalDate today);
}
