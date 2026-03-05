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

        if (requestDto != null && requestDto.getClass() == UserRegistrationRequestDto.class) {
            password = ((UserRegistrationRequestDto) requestDto).getPassword();
            repeatedPassword = ((UserRegistrationRequestDto) requestDto).getRepeatedPassword();
        }
        if (requestDto != null && requestDto.getClass() == UserUpdateRequestDto.class) {
            password = ((UserUpdateRequestDto) requestDto).getPassword();
            repeatedPassword = ((UserUpdateRequestDto) requestDto).getRepeatedPassword();
        }
        return password != null && password.equals(repeatedPassword);
    }
}
