package accommodation.booking.app.controller;

import accommodation.booking.app.dto.user.UserLoginRequestDto;
import accommodation.booking.app.dto.user.UserLoginResponseDto;
import accommodation.booking.app.dto.user.UserRegistrationRequestDto;
import accommodation.booking.app.dto.user.UserResponseDto;
import accommodation.booking.app.exception.RegistrationException;
import accommodation.booking.app.security.AuthenticationService;
import accommodation.booking.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication Controller",
        description = "Authentication related endpoints")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Allows users to register a new account")
    UserResponseDto register(@Valid @RequestBody UserRegistrationRequestDto request)
            throws RegistrationException {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Grants JWT tokens to authenticated users")
    public UserLoginResponseDto login(@Valid @RequestBody UserLoginRequestDto request) {
        return authenticationService.authenticate(request);
    }
}
