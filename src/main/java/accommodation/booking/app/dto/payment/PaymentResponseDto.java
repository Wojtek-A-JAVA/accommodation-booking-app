package accommodation.booking.app.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponseDto {
    private Long paymentId;
    private String status;
    private String message;

}
