package accommodation.booking.app.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpirationScheduler {
    private final BookingService bookingService;

    @Scheduled(cron = "5 0 0 * * *")
    public void expireBookings() {
        bookingService.expireOldBookings(LocalDate.now());
    }
}
