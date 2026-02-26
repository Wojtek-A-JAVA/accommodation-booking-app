package accommodation.booking.app.dto.user;

import accommodation.booking.app.dto.user.passwordvalidator.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@PasswordMatch(first = "password", second = "repeatedPassword",
        message = "Password do not match with repeated password")
@Getter
@Setter
@Accessors(chain = true)
public class UserRegistrationRequestDto {
    @NotBlank(message = "Cannot be empty")
    @Email(message = "Must be a valid email")
    private String email;
    @NotBlank(message = "Cannot be empty")
    private String firstName;
    @NotBlank(message = "Cannot be empty")
    private String lastName;
    @NotBlank(message = "Cannot be empty")
    @Size(min = 3, max = 20, message = "Min size is 3 and max size is 20 characters")
    private String password;
    @NotBlank(message = "Cannot be empty")
    private String repeatedPassword;
}
