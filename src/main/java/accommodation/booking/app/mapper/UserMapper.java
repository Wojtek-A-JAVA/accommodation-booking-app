package accommodation.booking.app.mapper;

import accommodation.booking.app.config.MapperConfig;
import accommodation.booking.app.dto.user.UserRegistrationRequestDto;
import accommodation.booking.app.dto.user.UserResponseDto;
import accommodation.booking.app.dto.user.UserRoleResponseDto;
import accommodation.booking.app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User toEntity(UserRegistrationRequestDto requestDto);

    @Mapping(target = "role", source = "role.roleName")
    UserResponseDto toDto(User user);

    @Mapping(target = "role", source = "role.roleName")
    UserRoleResponseDto toUserRoleResponseDto(User user);
}
