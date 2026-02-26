package accommodation.booking.app.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import accommodation.booking.app.model.Payment;
import accommodation.booking.app.model.Status;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void findAllByBookingId_User_Id_ReturnsList_Success() {
        List<Payment> payments = paymentRepository.findAllByBookingId_User_Id(2L);
        assertNotNull(payments);
        assertFalse(payments.isEmpty());
        assertTrue(payments.stream().allMatch(p -> p.getBookingId() != null
                && p.getBookingId().getUser() != null
                && p.getBookingId().getUser().getId().equals(2L)));
    }

    @Test
    void findBySessionId_Existing_ReturnsPayment_Success() {
        Optional<Payment> payment = paymentRepository.findBySessionId(
                "cs_test_a1ul8GYVbyk0MZL75ePtaVvC0PYm8TNXMwcq3Flcze0fZBIX4SvzhlJkIM"
        );
        assertTrue(payment.isPresent());
        assertNotNull(payment.get().getId());
    }

    @Test
    void findAllByBookingId_User_IdAndStatus_ReturnsPending_Success() {
        List<Payment> payments =
                paymentRepository.findAllByBookingId_User_IdAndStatus(2L, Status.PENDING);
        assertNotNull(payments);
        assertFalse(payments.isEmpty());
        assertTrue(payments.stream().allMatch(p -> p.getStatus() == Status.PENDING));
    }
}
