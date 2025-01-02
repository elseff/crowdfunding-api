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
public class CreateCommentRequest {
    @NotNull(message = "text не должно быть пустым")
    String text;
    @NotNull(message = "userId не должно быть пустым")
    Long userId;
}
