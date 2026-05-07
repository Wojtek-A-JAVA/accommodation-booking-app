package accommodation.booking.app.security.auth;

import accommodation.booking.app.dto.user.UserLoginRequestDto;
import accommodation.booking.app.dto.user.UserLoginResponseDto;
import accommodation.booking.app.exception.EntityNotFoundException;
import accommodation.booking.app.model.User;
import accommodation.booking.app.repository.UserRepository;
import accommodation.booking.app.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public UserLoginResponseDto authenticate(UserLoginRequestDto request) {

        Authentication authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        request.email().trim().toLowerCase(),
                        request.password()));

        User user = userRepository.findByEmail(authenticate.getName()).orElseThrow(
                () -> new EntityNotFoundException("User not found in database"));

        String token = jwtUtil.generateToken(user.getId(), authenticate.getName());

        return new UserLoginResponseDto(token);
    }
}
