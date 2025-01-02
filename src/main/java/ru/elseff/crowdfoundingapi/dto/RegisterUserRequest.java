package ru.elseff.crowdfoundingapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterUserRequest {
    @NotNull(message = "firstName должен быть заполнен")
    String firstName;

    @NotNull(message = "lastName должен быть заполнен")
    String lastName;

    @NotNull(message = "email должен быть заполнен")
    String email;

    @NotNull(message = "password должен быть заполнен")
    String password;
}
