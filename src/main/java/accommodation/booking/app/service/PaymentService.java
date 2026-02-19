package accommodation.booking.app.service;

import accommodation.booking.app.dto.payment.CreatePaymentRequestDto;
import accommodation.booking.app.dto.payment.PaymentDto;
import accommodation.booking.app.dto.payment.PaymentResponseDto;
import accommodation.booking.app.model.User;
import java.util.List;

public interface PaymentService {

    List<PaymentDto> getAllPaymentsByUserId(Long id, User user);

    PaymentDto createPaymentSession(CreatePaymentRequestDto createPaymentRequestDto, User user);

    PaymentResponseDto paymentSuccess(String sessionId);

    PaymentResponseDto paymentCancel(String sessionId);
}
