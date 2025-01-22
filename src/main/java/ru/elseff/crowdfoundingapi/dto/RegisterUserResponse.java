package ru.elseff.crowdfoundingapi.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterUserResponse {
    Long id;
    String firstName;
    String lastName;
    String email;
    Integer balance;
    String message;
}
