package ru.elseff.crowdfoundingapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
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
                                .id(p.getAuthor().getId())
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
                                        .createdAt(c.getCreatedAt())
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
    public CreateProjectResponse createProject(@RequestBody @Valid CreateProjectRequest request) {
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
    public ImageOperationResponse addImage(@PathVariable Long projectId,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestParam Long userId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("Project is not found");
        }
        Project project = projectOptional.get();
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User is not found");
        }
        if (!project.getAuthor().getId().equals(userOptional.get().getId())) {
            throw new IllegalArgumentException("Someone else project");
        }
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

        return ImageOperationResponse.builder()
                .name(file.getOriginalFilename())
                .message("Image successfully added to project")
                .build();
    }

    @DeleteMapping("/{projectId}/images/{imageId}")
    public ImageOperationResponse deleteImage(@PathVariable Long projectId,
                                              @PathVariable Long imageId,
                                              @RequestParam Long userId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("Project is not found");
        }
        Project project = projectOptional.get();
        Optional<Image> imageOptional = imageRepository.findById(imageId);
        if (imageOptional.isEmpty()) {
            throw new IllegalArgumentException("Image is not found");
        }
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User is not found");
        }
        if (!project.getAuthor().getId().equals(userOptional.get().getId())) {
            throw new IllegalArgumentException("Someone else project");
        }
        Image image = imageOptional.get();
        imageRepository.delete(image);

        return ImageOperationResponse.builder()
                .name(image.getName())
                .message("Image deleted successfully")
                .build();
    }

    @GetMapping(value = "/{projectId}/images/{imageId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> findImage(@PathVariable Long projectId,
                                              @PathVariable Long imageId) {
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

    @PostMapping("/{projectId}/support")
    @Transactional(rollbackFor = IllegalArgumentException.class)
    public SupportProjectResponse supportProject(@PathVariable Long projectId,
                                                 @RequestParam Long userId,
                                                 @RequestParam Integer amount) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("Project is not found");
        }
        Project project = projectOptional.get();
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User is not found");
        }
        User user = userOptional.get();
        if (project.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot support your own project");
        }
        if (user.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        user.setBalance(user.getBalance() - amount);
        project.getAuthor().setBalance(project.getAuthor().getBalance() + amount);
        userRepository.save(user);
        projectRepository.save(project);

        return SupportProjectResponse.builder()
                .amount(amount)
                .projectName(project.getName())
                .message("Successfully supported project")
                .build();
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId,
                                                @RequestParam Long userId) throws JsonProcessingException {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("Project is not found");
        }
        Project project = projectOptional.get();
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User is not found");
        }
        if (!project.getAuthor().getId().equals(userOptional.get().getId())) {
            throw new IllegalArgumentException("Someone else project");
        }
        projectRepository.delete(project);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString("Проект успешно удалён"));
    }

}
