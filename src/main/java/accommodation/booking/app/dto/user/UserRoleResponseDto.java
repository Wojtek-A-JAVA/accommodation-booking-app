package accommodation.booking.app.dto.user;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class UserRoleResponseDto {
    private Long id;
    private String role;
}
