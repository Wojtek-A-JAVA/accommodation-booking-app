package accommodation.booking.app.dto.payment;

import java.math.BigDecimal;

public record PaymentDto(
        Long id,
        Long bookingId,
        String status,
        BigDecimal amountToPay,
        String sessionId,
        String sessionUrl
) {
}
