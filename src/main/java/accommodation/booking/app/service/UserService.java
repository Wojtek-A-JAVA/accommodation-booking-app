package accommodation.booking.app.service;

import accommodation.booking.app.dto.user.UserRegistrationRequestDto;
import accommodation.booking.app.dto.user.UserResponseDto;
import accommodation.booking.app.dto.user.UserRoleResponseDto;
import accommodation.booking.app.dto.user.UserUpdateRequestDto;
import accommodation.booking.app.exception.RegistrationException;
import accommodation.booking.app.model.User;

public interface UserService {

    UserResponseDto register(UserRegistrationRequestDto request) throws RegistrationException;

    UserRoleResponseDto updateUserRole(Long id, User user);

    UserResponseDto getLoggedInUserInfo(User user);

    UserResponseDto updateLoggedInUserInfo(UserUpdateRequestDto request, User user);
}

