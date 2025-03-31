package ru.elseff.crowdfoundingapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectDto {
    Long id;
    String name;
    String description;
    ProjectCategoryDto category;
    int target;
    int collected;
    UserDto author;
    List<ImageDto> images;
    List<CommentDto> comments;
    LocalDateTime createdAt;
    boolean closed;
    LocalDateTime closedAt;
}
