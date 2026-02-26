package accommodation.booking.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import accommodation.booking.app.model.Booking;
import accommodation.booking.app.model.Status;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void findByUserIdAndStatus_ReturnsList_Success() {
        List<Booking> bookings = bookingRepository.findByUserIdAndStatus(2L, Status.CONFIRMED);
        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
        assertTrue(bookings.stream().anyMatch(b -> b.getId() != null && b.getId().equals(2L)));
        assertEquals(2L, bookings.get(0).getId());
    }

    @Test
    void findByUserId_ReturnsList_Success() {
        List<Booking> bookings = bookingRepository.findByUserId(2L);
        assertFalse(bookings.isEmpty());
        assertEquals(2, bookings.size());
        assertEquals(2L, bookings.get(1).getId());
    }

    @Test
    void findReservedAccommodations_ReturnsBooking_Success() {
        List<Booking> reserved = bookingRepository.findReservedAccommodations(
                1L,
                LocalDate.of(2027, 1, 15),
                EnumSet.of(Status.CANCELED, Status.EXPIRED)
        );

        assertFalse(reserved.isEmpty());
        assertTrue(reserved.stream().anyMatch(b -> b.getAccommodation().getId().equals(1L)));
    }

    @Test
    void findBookingsToExpire_ReturnsBookings_Success() {
        List<Booking> toExpire = bookingRepository.findBookingsToExpire(
                LocalDate.of(2027, 2, 1),
                EnumSet.of(Status.PENDING, Status.CONFIRMED)
        );

        assertFalse(toExpire.isEmpty());
        Booking booking = toExpire.getFirst();
        assertNotNull(booking.getAccommodation());
        assertNotNull(booking.getUser());
        assertNotNull(booking.getAccommodation().getId());
        assertNotNull(booking.getUser().getId());
    }

    @Test
    void checkForActiveBookingsOnDate_ReturnsTrue() {
        boolean result = bookingRepository.checkForActiveBookingsOnDate(
                1L,
                LocalDate.of(2027, 1, 15),
                EnumSet.of(Status.PENDING, Status.CONFIRMED)
        );

        assertTrue(result);
    }
}
