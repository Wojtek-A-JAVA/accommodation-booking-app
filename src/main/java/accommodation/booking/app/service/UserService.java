package accommodation.booking.app.service;

import accommodation.booking.app.dto.user.UserRegistrationRequestDto;
import accommodation.booking.app.dto.user.UserResponseDto;
import accommodation.booking.app.dto.user.UserRoleResponseDto;
import accommodation.booking.app.dto.user.UserUpdateRequestDto;
import accommodation.booking.app.exception.RegistrationException;

public interface UserService {

    UserResponseDto register(UserRegistrationRequestDto request) throws RegistrationException;

    UserRoleResponseDto updateUserRole(Long id, String userEmail);

    UserResponseDto getLoggedInUserInfo(String userEmail);

    UserResponseDto updateLoggedInUserInfo(UserUpdateRequestDto request, String userEmail);
}

