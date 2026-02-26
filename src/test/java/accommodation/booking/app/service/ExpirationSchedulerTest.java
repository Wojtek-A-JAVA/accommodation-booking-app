package accommodation.booking.app.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ExpirationSchedulerTest {

    @Test
    void expireBookings_CallsBookingServiceWithTodayDate() {
        BookingService bookingService = mock(BookingService.class);
        ExpirationScheduler scheduler = new ExpirationScheduler(bookingService);

        LocalDate before = LocalDate.now();

        scheduler.expireBookings();

        LocalDate after = LocalDate.now();

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(bookingService, times(1)).expireOldBookings(dateCaptor.capture());

        LocalDate actual = dateCaptor.getValue();
        assertFalse(actual.isBefore(before), "Date passed to service must be >= date before call");
        assertTrue(!actual.isAfter(after), "Date passed to service must be <= date after call");
    }
}
