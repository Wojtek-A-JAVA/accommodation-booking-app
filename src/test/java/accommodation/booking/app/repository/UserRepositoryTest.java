package accommodation.booking.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import accommodation.booking.app.model.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_ReturnsUser_Success() {
        Optional<User> user = userRepository.findByEmail("admin@booking.app");
        assertTrue(user.isPresent());
        assertEquals("admin@booking.app", user.get().getEmail());
    }

    @Test
    void findByEmail_ReturnsEmpty_Success() {
        assertTrue(userRepository.findByEmail("test@test.com").isEmpty());
    }
}
