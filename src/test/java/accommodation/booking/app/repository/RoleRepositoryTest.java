package accommodation.booking.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import accommodation.booking.app.model.Role;
import accommodation.booking.app.model.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByRoleName_ReturnsRole_Success() {
        Role role = roleRepository.findByRoleName(RoleName.ADMIN).orElseThrow();
        assertNotNull(role.getId());
        assertEquals(RoleName.ADMIN, role.getRoleName());
        assertEquals("ROLE_ADMIN", role.getAuthority());
    }
}
