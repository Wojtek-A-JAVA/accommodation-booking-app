package accommodation.booking.app.service;

import accommodation.booking.app.dto.booking.BookingDto;
import accommodation.booking.app.dto.booking.BookingUpdateRequestDto;
import accommodation.booking.app.dto.booking.CreateBookingRequestDto;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingDto createBooking(@Valid CreateBookingRequestDto bookingRequestDtoDto, String userEmail);

    List<BookingDto> getBookingsByUserIdAndStatus(Long id, String status);

    List<BookingDto> getUserBookings(String userEmail);

    BookingDto getBookingById(Long id);

    BookingDto updateBooking(Long id, BookingUpdateRequestDto bookingUpdateRequestDto,
                             String userEmail);

    void deleteBooking(Long id);

    void expireOldBookings(LocalDate today);
}
