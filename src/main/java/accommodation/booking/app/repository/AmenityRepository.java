package accommodation.booking.app.repository;

import accommodation.booking.app.model.Amenity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    Optional<Amenity> findById(Long id);
}
