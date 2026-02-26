package accommodation.booking.app.repository;

import accommodation.booking.app.model.Booking;
import accommodation.booking.app.model.Status;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdAndStatus(Long userId, Status status);

    List<Booking> findByUserId(Long userId);

    @Query("""
            select b from Booking b
            where b.accommodation.id = :accommodationId
              and b.checkInDate <= :date
              and b.checkOutDate > :date
              and b.status not in :nonReservedStatuses     
            """)
    List<Booking> findReservedAccommodations(Long accommodationId, LocalDate date,
                                             Set<Status> nonReservedStatuses);

    @Query("""

            select b
             from Booking b
             join fetch b.accommodation a
             join fetch b.user u
            where b.checkOutDate <= :checkOut
            and b.status in :activeStatuses
            """)
    List<Booking> findBookingsToExpire(@Param("checkOut") LocalDate today,
                                       @Param("activeStatuses") Set<Status> activeStatuses);

    @Query("""
            select (count(b) > 0)
            from Booking b
            where b.accommodation.id = :accommodationId
              and b.checkInDate <= :date
              and b.checkOutDate >= :date
              and b.status in :activeStatuses
            """)
    boolean checkForActiveBookingsOnDate(
            @Param("accommodationId") Long accommodationId,
            @Param("date") LocalDate date,
            @Param("activeStatuses") Set<Status> activeStatuses
    );
}
