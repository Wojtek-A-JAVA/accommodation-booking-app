package accommodation.booking.app.repository;

import accommodation.booking.app.model.Accommodation;
import accommodation.booking.app.model.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    Optional<Accommodation> findByTypeAndSizeAndLocationId(Type type, String size, Long locationId);

    Optional<Accommodation> findById(Long id);

    @Query("""
            select a
            from Accommodation a
            where (
                :city is null
                or
                :city = ''
                or
                a.location.city = :city
            )            
            and a.id not in (
                select b.accommodation.id
                from Booking b
                where
                    b.isDeleted = false
                    and
                    b.checkInDate < :checkOut
                    and
                    b.checkOutDate > :checkIn
            )
            """)
    List<Accommodation> findAvailableAccommodations(@Param("city") String city,
                                                    @Param("checkIn") LocalDate checkIn,
                                                    @Param("checkOut") LocalDate checkOut);
}
