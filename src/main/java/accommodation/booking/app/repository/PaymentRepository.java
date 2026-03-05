package accommodation.booking.app.repository;

import accommodation.booking.app.model.Payment;
import accommodation.booking.app.model.Status;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByBookingId_User_Id(Long userId);

    Optional<Payment> findBySessionId(String sessionId);

    List<Payment> findAllByBookingId_User_IdAndStatus(Long userId, Status status);
}
