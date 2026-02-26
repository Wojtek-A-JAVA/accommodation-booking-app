package accommodation.booking.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import accommodation.booking.app.model.Amenity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AmenityRepositoryTest {

    @Autowired
    private AmenityRepository amenityRepository;

    @Test
    void findById_ReturnsAmenity() {
        Amenity amenity = amenityRepository.findById(1L).orElseThrow();
        assertEquals(1L, amenity.getId());
        assertNotNull(amenity.getName());
    }

    @Test
    void findById_ReturnsEmpty() {
        assertTrue(amenityRepository.findById(99999L).isEmpty());
    }
}
