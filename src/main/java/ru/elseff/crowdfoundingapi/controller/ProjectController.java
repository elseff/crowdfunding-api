package ru.elseff.crowdfoundingapi.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.elseff.crowdfoundingapi.dao.entity.Comment;
import ru.elseff.crowdfoundingapi.dao.entity.Image;
import ru.elseff.crowdfoundingapi.dao.entity.Project;
import ru.elseff.crowdfoundingapi.dao.entity.User;
import ru.elseff.crowdfoundingapi.dao.repository.CommentRepository;
import ru.elseff.crowdfoundingapi.dao.repository.ImageRepository;
import ru.elseff.crowdfoundingapi.dao.repository.ProjectRepository;
import ru.elseff.crowdfoundingapi.dao.repository.UserRepository;
import ru.elseff.crowdfoundingapi.dto.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Validated
@Transactional
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController {

    CommentRepository commentRepository;
    ProjectRepository projectRepository;
    ImageRepository imageRepository;
    UserRepository userRepository;

    @GetMapping
    public List<ProjectDto> findAll() {
        return projectRepository.findAll()
                .stream()
                .map(p -> ProjectDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .author(UserDto.builder()
                                .firstName(p.getAuthor().getFirstName())
                                .lastName(p.getAuthor().getLastName())
                                .build())
                        .images(p.getImages()
                                .stream()
                                .map(i -> ImageDto.builder()
                                        .id(i.getId())
                                        .name(i.getName())
                                        .build())
                                .toList())
                        .comments(p.getComments()
                                .stream()
                                .map(c -> CommentDto.builder()
                                        .id(c.getId())
                                        .text(c.getText())
                                        .user(UserDto.builder()
                                                .id(c.getUser().getId())
                                                .firstName(c.getUser().getFirstName())
                                                .lastName(c.getUser().getLastName())
                                                .build())
                                        .build())
                                .toList())
                        .build())
                .toList();
    }

    @PostMapping
    public CreateProjectResponse createProject(@RequestBody CreateProjectRequest request) {
        Optional<User> userOptional = userRepository.findById(request.getAuthorId());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("user with id " + request.getAuthorId() + " is not found");
        }
        User user = userOptional.get();
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .author(user)
                .build();
        projectRepository.save(project);

        return CreateProjectResponse.builder()
                .name(project.getName())
                .message("Проект успешно создан")
                .build();
    }

    @PostMapping("/{projectId}/images/upload")
    public AddImageResponse addImage(@PathVariable Long projectId,
                                     @RequestParam("file") MultipartFile file) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("Project is not found");
        }
        Project project = projectOptional.get();

        try {
            Image image = Image.builder()
                    .name(file.getOriginalFilename())
                    .project(project)
                    .data(file.getResource().getContentAsByteArray())
                    .build();
            imageRepository.save(image);
        } catch (IOException e) {
            throw new IllegalArgumentException("could not save file");
        }

        return AddImageResponse.builder()
                .name(file.getOriginalFilename())
                .message("Image successfully added to project")
                .build();
    }

    @GetMapping(value = "/{projectId}/images/{imageId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> findImage(@PathVariable Long projectId, @PathVariable Long imageId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("Project is not found");
        }
        Project project = projectOptional.get();
        Image image = project.getImages()
                .stream()
                .filter(i -> i.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("image not found"));

        ByteArrayResource resource = new ByteArrayResource(image.getData());

        return ResponseEntity.ok(resource);
    }

    @PostMapping("/{projectId}/comments")
    public CreateCommentResponse createComment(@PathVariable Long projectId,
                                               @RequestBody @Valid CreateCommentRequest request) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("Project is not found");
        }
        Project project = projectOptional.get();
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User is not found");
        }
        User user = userOptional.get();
        Comment comment = Comment.builder()
                .text(request.getText())
                .project(project)
                .user(user)
                .build();
        commentRepository.save(comment);

        return CreateCommentResponse.builder()
                .projectId(projectId)
                .text(comment.getText())
                .message("Comment created successfully")
                .build();
    }
}
