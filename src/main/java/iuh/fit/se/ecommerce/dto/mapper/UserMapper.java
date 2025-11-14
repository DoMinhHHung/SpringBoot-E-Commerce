package iuh.fit.se.ecommerce.dto.mapper;

import iuh.fit.se.ecommerce.dto.request.RegisterRequest;
import iuh.fit.se.ecommerce.dto.response.UserResponse;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.AuthProvider;
import iuh.fit.se.ecommerce.entity.enums.Role;
import org.mapstruct.*;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "dob", source = "dob", qualifiedByName = "toLocalDate")
    User toEntity(RegisterRequest req);

    @Mapping(target = "avatar", source = "avatar")
    UserResponse toResponse(User user);

    @Named("toLocalDate")
    static LocalDate toLocalDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s);
    }

    @AfterMapping
    default void afterMapping(@MappingTarget User user, RegisterRequest request) {
        if (user.getRole() == null) user.setRole(Role.USER);
        if (user.getAuthProvider() == null) user.setAuthProvider(AuthProvider.LOCAL);
    }
}