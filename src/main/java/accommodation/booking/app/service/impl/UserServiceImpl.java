package accommodation.booking.app.service.impl;

import accommodation.booking.app.dto.user.UserRegistrationRequestDto;
import accommodation.booking.app.dto.user.UserResponseDto;
import accommodation.booking.app.dto.user.UserRoleResponseDto;
import accommodation.booking.app.dto.user.UserUpdateRequestDto;
import accommodation.booking.app.exception.EntityNotFoundException;
import accommodation.booking.app.exception.RegistrationException;
import accommodation.booking.app.exception.RoleNotFoundExpectation;
import accommodation.booking.app.mapper.UserMapper;
import accommodation.booking.app.model.Role;
import accommodation.booking.app.model.RoleName;
import accommodation.booking.app.model.User;
import accommodation.booking.app.repository.RoleRepository;
import accommodation.booking.app.repository.UserRepository;
import accommodation.booking.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto request)
            throws RegistrationException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegistrationException("Email is already in use");
        }
        Role role = roleRepository.findByRoleName(RoleName.CUSTOMER)
                .orElseThrow(() ->
                        new RegistrationException("Customer role not found in database"));
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserRoleResponseDto updateUserRole(Long id, User user) {
        if (!user.getId().equals(id)) {
            throw new IllegalArgumentException("Logged user id doesn't match with id " + id);
        }
        User userToUpdate = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User not found with id: " + id));

        if (userToUpdate.getRole().getRoleName().equals(RoleName.ADMIN)) {
            userToUpdate.setRole(roleRepository.findByRoleName(RoleName.CUSTOMER).orElseThrow(
                    () -> new RoleNotFoundExpectation("Customer role not found in database")));
        } else if (userToUpdate.getRole().getRoleName().equals(RoleName.CUSTOMER)) {
            userToUpdate.setRole(roleRepository.findByRoleName(RoleName.ADMIN).orElseThrow(
                    () -> new RoleNotFoundExpectation("Admin role not found in database")));
        }
        userRepository.save(userToUpdate);
        return userMapper.toUserRoleResponseDto(userToUpdate);
    }

    @Override
    public UserResponseDto getLoggedInUserInfo(User user) {
        User loggedUser = userRepository.findByEmail(user.getEmail()).orElseThrow(
                () -> new EntityNotFoundException("User not found in database"));
        return userMapper.toDto(loggedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateLoggedInUserInfo(UserUpdateRequestDto request, User user) {
        User loggedUser = userRepository.findByEmail(user.getEmail()).orElseThrow(
                () -> new EntityNotFoundException("User not found in database"));
        if (request.getPassword() != null) {
            loggedUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getFirstName() != null) {
            loggedUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            loggedUser.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            loggedUser.setEmail(request.getEmail());
        }
        return userMapper.toDto(userRepository.save(loggedUser));
    }
}
