package accommodation.booking.app.repository;

import accommodation.booking.app.model.Location;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findById(Long id);
}
