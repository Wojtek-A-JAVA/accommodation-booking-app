package accommodation.booking.app.security;

import accommodation.booking.app.dto.user.UserLoginRequestDto;
import accommodation.booking.app.dto.user.UserLoginResponseDto;
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

    public UserLoginResponseDto authenticate(UserLoginRequestDto request) {

        Authentication authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        request.email().trim().toLowerCase(),
                        request.password()));

        String token = jwtUtil.generateToken(authenticate.getName());

        return new UserLoginResponseDto(token);
    }
}
