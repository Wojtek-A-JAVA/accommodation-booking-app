package accommodation.booking.app.controller;

import accommodation.booking.app.dto.payment.CreatePaymentRequestDto;
import accommodation.booking.app.dto.payment.PaymentDto;
import accommodation.booking.app.dto.payment.PaymentResponseDto;
import accommodation.booking.app.model.User;
import accommodation.booking.app.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment Controller", description = "Payment related endpoints")
@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @Operation(summary = "Get all payments by user id",
            description = "Allows to get all payments by user id, admin can get any user payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public List<PaymentDto> getPaymentsByUserId(
            @RequestParam(name = "user_id") Long userId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return paymentService.getAllPaymentsByUserId(userId, user);
    }

    @PostMapping
    @Operation(summary = "Create payment session",
            description = "Creates payment session for user")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public PaymentDto createPaymentSession(
            @Valid @RequestBody CreatePaymentRequestDto requestDto, Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return paymentService.createPaymentSession(requestDto, user);
    }

    @GetMapping("/success")
    @Operation(summary = "Payment success",
            description = "Handles Stripe Checkout session success")
    public PaymentResponseDto paymentSuccess(
            @RequestParam(name = "session_id") String sessionId
    ) {
        return paymentService.paymentSuccess(sessionId);
    }

    @GetMapping("/cancel")
    @Operation(summary = "Payment cancel", description = "Handles Stripe Checkout session cancel")
    public PaymentResponseDto paymentCancel(
            @RequestParam(name = "session_id") String sessionId
    ) {
        return paymentService.paymentCancel(sessionId);
    }
}
