package accommodation.booking.app.controller;

import accommodation.booking.app.dto.user.UserResponseDto;
import accommodation.booking.app.dto.user.UserRoleResponseDto;
import accommodation.booking.app.dto.user.UserUpdateRequestDto;
import accommodation.booking.app.model.User;
import accommodation.booking.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Controller", description = "User related endpoints")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/users")
public class UserController {

    private final UserService userService;

    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role",
            description = "Enables users to update their roles, providing role-based access")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public UserRoleResponseDto updateUserRole(@PathVariable Long id,
                                              Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userService.updateUserRole(id, user);
    }

    @GetMapping("/me")
    @Operation(summary = "Get logged in user info",
            description = "Provides user information for logged in user")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public UserResponseDto getLoggedInUserInfo(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userService.getLoggedInUserInfo(user);
    }

    @PatchMapping("/me")
    @Operation(summary = "Update logged in user info",
            description = "Updates user information for logged in user")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public UserResponseDto updateLoggedInUserInfo(Authentication authentication, @Valid
            @RequestBody UserUpdateRequestDto request) {
        User user = (User) authentication.getPrincipal();
        return userService.updateLoggedInUserInfo(request, user);
    }
}
