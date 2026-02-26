package accommodation.booking.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import accommodation.booking.app.model.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepository;

    @Test
    void findById_ReturnsLocation() {
        Location location = locationRepository.findById(1L).orElseThrow();
        assertEquals(1L, location.getId());
        assertNotNull(location.getCountry());
        assertNotNull(location.getCity());
        assertNotNull(location.getStreet());
    }

    @Test
    void findById_ReturnsEmpty() {
        assertTrue(locationRepository.findById(99999L).isEmpty());
    }
}
