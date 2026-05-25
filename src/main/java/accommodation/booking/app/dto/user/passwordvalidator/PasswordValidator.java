package accommodation.booking.app.dto.user.passwordvalidator;

import accommodation.booking.app.dto.user.UserRegistrationRequestDto;
import accommodation.booking.app.dto.user.UserUpdateRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<PasswordMatch,
        Object> {

    private String password;
    private String repeatedPassword;

    @Override
    public boolean isValid(Object requestDto,
                           ConstraintValidatorContext constraintValidatorContext) {

        if (requestDto instanceof UserUpdateRequestDto dto) {
            password = dto.getPassword();
            repeatedPassword = dto.getRepeatedPassword();
        }
        if (requestDto instanceof UserRegistrationRequestDto dto) {
            password = dto.getPassword();
            repeatedPassword = dto.getRepeatedPassword();
        }
        if (password == null && repeatedPassword == null) {
            return true;
        }
        if (password == null || repeatedPassword == null) {
            return false;
        }
        return password.equals(repeatedPassword);
    }
}
