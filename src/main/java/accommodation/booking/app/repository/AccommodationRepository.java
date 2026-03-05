package accommodation.booking.app.repository;

import accommodation.booking.app.model.Accommodation;
import accommodation.booking.app.model.Type;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    Optional<Accommodation> findByTypeAndSizeAndLocationId(Type type, String size, Long locationId);

    Optional<Accommodation> findById(Long id);
}
