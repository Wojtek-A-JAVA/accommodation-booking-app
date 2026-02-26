package accommodation.booking.app.service;

import accommodation.booking.app.dto.payment.CreatePaymentRequestDto;
import accommodation.booking.app.dto.payment.PaymentDto;
import accommodation.booking.app.dto.payment.PaymentResponseDto;
import java.util.List;

public interface PaymentService {

    List<PaymentDto> getAllPaymentsByUserId(Long id, String userEmail);

    PaymentDto createPaymentSession(CreatePaymentRequestDto createPaymentRequestDto,
                                    String userEmail);

    PaymentResponseDto paymentSuccess(String sessionId);

    PaymentResponseDto paymentCancel(String sessionId);
}
