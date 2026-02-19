package accommodation.booking.app.controller;

import accommodation.booking.app.dto.booking.BookingDto;
import accommodation.booking.app.dto.booking.BookingUpdateRequestDto;
import accommodation.booking.app.dto.booking.CreateBookingRequestDto;
import accommodation.booking.app.model.User;
import accommodation.booking.app.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Booking Controller", description = "Booking related endpoints")
@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping()
    @Operation(summary = "Create booking", description = "Allows user to book accommodation")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public BookingDto createBooking(
            @RequestBody @Valid CreateBookingRequestDto bookingRequestDtoDto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return bookingService.createBooking(bookingRequestDtoDto, user);
    }

    @GetMapping({"", "/"})
    @Operation(summary = "Get bookings list",
            description = "Retrieves bookings based on user ID and their status - only managers")
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingDto> getBookingsByUserIdAndStatus(
            @RequestParam("user_id") Long id, @RequestParam String status) {
        return bookingService.getBookingsByUserIdAndStatus(id, status);
    }

    @GetMapping("/my")
    @Operation(summary = "Get user bookings list",
            description = "Retrieves bookings for logged in user")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public List<BookingDto> getUserBookings(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return bookingService.getUserBookings(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID",
            description = "Retrieves booking by ID - only managers")
    @PreAuthorize("hasRole('ADMIN')")
    public BookingDto getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update booking",
            description = "Allows to update booking")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public BookingDto updateBooking(
            @PathVariable Long id,
            @RequestBody @Valid BookingUpdateRequestDto bookingUpdateRequestDto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return bookingService.updateBooking(id, bookingUpdateRequestDto, user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete booking",
            description = "Allows to delete booking - only managers")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
    }
}
