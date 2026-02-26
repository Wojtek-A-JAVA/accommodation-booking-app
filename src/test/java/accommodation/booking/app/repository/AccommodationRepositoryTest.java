package accommodation.booking.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import accommodation.booking.app.model.Accommodation;
import accommodation.booking.app.model.Type;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccommodationRepositoryTest {

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Test
    void findByTypeAndSizeAndLocationId_WhenSeededRowExists_ReturnsAccommodation() {
        Optional<Accommodation> accommodation =
                accommodationRepository.findByTypeAndSizeAndLocationId(
                        Type.APARTMENT, "Studio", 1L
                );

        assertTrue(accommodation.isPresent());
        assertNotNull(accommodation.get().getId());
        assertEquals(Type.APARTMENT, accommodation.get().getType());
        assertEquals("Studio", accommodation.get().getSize());
        assertNotNull(accommodation.get().getLocation());
        assertEquals(1L, accommodation.get().getLocation().getId());
    }

    @Test
    void findByTypeAndSizeAndLocationId_WhenNoRow_ReturnsEmpty() {
        assertTrue(accommodationRepository.findByTypeAndSizeAndLocationId(
                Type.HOUSE, "", 999L
        ).isEmpty());
    }

    @Test
    void findById_ReturnsAccommodation() {
        Optional<Accommodation> accommodation =
                accommodationRepository.findById(1L);
        assertTrue(accommodation.isPresent());
        assertNotNull(accommodation.get().getId());
        assertEquals(Type.APARTMENT, accommodation.get().getType());
        assertEquals("Studio", accommodation.get().getSize());
        assertNotNull(accommodation.get().getLocation());
        assertEquals(1L, accommodation.get().getLocation().getId());
    }

}