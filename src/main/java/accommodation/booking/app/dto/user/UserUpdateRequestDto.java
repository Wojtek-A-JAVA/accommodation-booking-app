package accommodation.booking.app.dto.user;

import accommodation.booking.app.dto.user.passwordvalidator.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@PasswordMatch(first = "password", second = "repeatedPassword",
        message = "Password do not match with repeated password")
@Getter
@Setter
@Accessors(chain = true)
public class UserUpdateRequestDto {
    @Email(message = "Must be a valid email")
    private String email;
    private String firstName;
    private String lastName;
    @Size(min = 3, max = 20, message = "Min size is 3 and max size is 20 characters")
    private String password;
    private String repeatedPassword;
}
