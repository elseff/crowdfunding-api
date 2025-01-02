package ru.elseff.crowdfoundingapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProjectRequest {
    @NotNull(message = "name не должно быть пустым")
    String name;
    @NotNull(message = "description не должно быть пустым")
    String description;
    @NotNull(message = "authorId не должно быть пустым")
    Long authorId;
}
