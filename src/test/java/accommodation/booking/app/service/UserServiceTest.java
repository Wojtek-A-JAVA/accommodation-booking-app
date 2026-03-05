package accommodation.booking.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import accommodation.booking.app.dto.user.UserRegistrationRequestDto;
import accommodation.booking.app.dto.user.UserResponseDto;
import accommodation.booking.app.dto.user.UserRoleResponseDto;
import accommodation.booking.app.dto.user.UserUpdateRequestDto;
import accommodation.booking.app.exception.RegistrationException;
import accommodation.booking.app.mapper.UserMapper;
import accommodation.booking.app.model.Role;
import accommodation.booking.app.model.RoleName;
import accommodation.booking.app.model.User;
import accommodation.booking.app.repository.RoleRepository;
import accommodation.booking.app.repository.UserRepository;
import accommodation.booking.app.service.impl.UserServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl service;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;

    @Test
    void register_WhenEmailAlreadyExists_Throws() {
        UserRegistrationRequestDto request = mock(UserRegistrationRequestDto.class);

        when(request.getEmail()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

        assertThrows(RegistrationException.class, () -> service.register(request));
        verify(userRepository).findByEmail("test@test.com");
        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    void updateUserRole_WhenUserRoleIsAdmin_SwitchesToCustomer() {
        Long id = 1L;
        String userEmail = "admin@test.com";
        User loggedUser = new User().setId(id).setEmail(userEmail);
        Role adminRole = new Role().setRoleName(RoleName.ADMIN);
        Role customerRole = new Role().setRoleName(RoleName.CUSTOMER);
        User user = new User().setId(id).setEmail(userEmail).setRole(adminRole);
        UserRoleResponseDto responseDto = new UserRoleResponseDto().setId(id).setRole("CUSTOMER");

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(loggedUser));
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(RoleName.CUSTOMER))
                .thenReturn(Optional.of(customerRole));
        when(userMapper.toUserRoleResponseDto(user)).thenReturn(responseDto);

        UserRoleResponseDto actual = service.updateUserRole(id, userEmail);

        assertNotNull(actual);
        assertEquals(id, actual.getId());
        assertEquals("CUSTOMER", actual.getRole());

        verify(userRepository).save(user);
        assertEquals(RoleName.CUSTOMER, user.getRole().getRoleName());
    }

    @Test
    void getLoggedInUserInfo_ReturnsDto() {
        String userEmail = "user@test.com";
        User user = new User()
                .setId(3L)
                .setEmail(userEmail)
                .setFirstName("James")
                .setLastName("Doe")
                .setPassword("password")
                .setRole(new Role().setRoleName(RoleName.CUSTOMER));
        UserResponseDto responseDto = new UserResponseDto()
                .setId(3L)
                .setEmail(userEmail)
                .setFirstName("James")
                .setLastName("Doe")
                .setRole(RoleName.CUSTOMER.name());

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto actual = service.getLoggedInUserInfo(userEmail);

        assertNotNull(actual);
        assertEquals(3L, actual.getId());
        assertEquals(userEmail, actual.getEmail());
        assertEquals("CUSTOMER", actual.getRole());

        verify(userRepository).findByEmail(userEmail);
        verify(userMapper).toDto(user);
    }

    @Test
    void updateLoggedInUserInfo_WhenAllFieldsProvided_UpdatesAndEncodesPassword() {
        String userEmail = "james@google.com";
        User userFromGetUser = new User().setId(3L).setEmail(userEmail);
        User loggedUser = new User()
                .setId(3L)
                .setEmail(userEmail)
                .setFirstName("James")
                .setLastName("Doe")
                .setPassword("oldPassword");
        UserUpdateRequestDto userUpdateRequestDto = new UserUpdateRequestDto()
                .setEmail("jamie@google.com")
                .setFirstName("Jamie")
                .setLastName("Test")
                .setPassword("password")
                .setRepeatedPassword("password");

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(userFromGetUser))
                .thenReturn(Optional.of(loggedUser));
        when(passwordEncoder.encode("password")).thenReturn("NEW_HASH");
        when(userRepository.save(loggedUser)).thenReturn(loggedUser);

        UserResponseDto userResponseDto = new UserResponseDto().setId(3L)
                .setEmail("jamie@google.com").setFirstName("Jamie").setLastName("Test");

        when(userMapper.toDto(loggedUser)).thenReturn(userResponseDto);

        UserResponseDto actual = service.updateLoggedInUserInfo(userUpdateRequestDto, userEmail);

        assertNotNull(actual);
        assertEquals(3L, actual.getId());
        assertEquals("jamie@google.com", actual.getEmail());
        assertEquals("Jamie", actual.getFirstName());
        assertEquals("Test", actual.getLastName());

        assertEquals("NEW_HASH", loggedUser.getPassword());
        assertEquals("jamie@google.com", loggedUser.getEmail());
        assertEquals("Jamie", loggedUser.getFirstName());
        assertEquals("Test", loggedUser.getLastName());

        verify(passwordEncoder).encode("password");
        verify(userRepository).save(loggedUser);
        verify(userMapper).toDto(loggedUser);
    }
}
