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
import ru.elseff.crowdfoundingapi.dao.entity.*;
import ru.elseff.crowdfoundingapi.dao.repository.*;
import ru.elseff.crowdfoundingapi.dto.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
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
    ProjectCategoryRepository projectCategoryRepository;
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
                        .target(p.getTarget())
                        .collected(p.getCollected())
                        .createdAt(p.getCreatedAt())
                        .closed(p.isClosed())
                        .closedAt(p.getClosedAt())
                        .category(ProjectCategoryDto.builder()
                                .id(p.getCategory().getId())
                                .name(p.getCategory().getName())
                                .build())
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
                                .sorted(Comparator.comparing(CommentDto::getCreatedAt).reversed())
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

        Optional<ProjectCategory> categoryOptional = projectCategoryRepository.findByName(request.getCategory());
        if (categoryOptional.isEmpty()) {
            throw new IllegalArgumentException("category with name " + request.getCategory() + " is not found");
        }
        ProjectCategory category = categoryOptional.get();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .author(user)
                .target(request.getTarget())
                .category(category)
                .build();
        this.projectRepository.save(project);

        return CreateProjectResponse.builder()
                .name(project.getName())
                .message("Проект успешно создан")
                .build();
    }

    @PostMapping(
            value = "/{projectId}/images/upload/{userId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE // Явное указание
    )
    public ImageOperationResponse addImage(@PathVariable Long projectId,
                                           @RequestPart("file") MultipartFile[] files,
                                           @PathVariable Long userId) {
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
        if (files.length == 0) {
            throw new IllegalArgumentException("file is empty");
        }
        try {
            for (MultipartFile file : files) {
                Image image = Image.builder()
                        .name(file.getOriginalFilename())
                        .project(project)
                        .data(file.getResource().getContentAsByteArray())
                        .build();
                imageRepository.save(image);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("could not save file");
        }

        return ImageOperationResponse.builder()
                .name(files[0].getOriginalFilename())
                .message("Image successfully added to project")
                .build();
    }

    @Transactional
    @DeleteMapping("/{projectId}/images/{imageId}")
    public ImageOperationResponse deleteImage(@PathVariable Long projectId,
                                              @PathVariable Long imageId,
                                              @RequestParam Long userId) {
        // Поиск проекта
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project is not found"));

        // Поиск изображения
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image is not found"));

        // Поиск пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not found"));

        // Проверка прав доступа
        if (!project.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Someone else project");
        }

        // Удаление изображения из проекта (если есть связь)
        project.getImages().remove(image);
        projectRepository.save(project);

        // Удаление изображения
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
        project.setCollected(project.getCollected() + amount);
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

    @PostMapping("/{projectId}/close")
    @Transactional(rollbackFor = IllegalArgumentException.class)
    public ResponseEntity<String> closeProject(@PathVariable Long projectId,
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
        User user = userOptional.get();
        if (!project.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Someone else project");
        }

        project.setClosed(true);
        project.setClosedAt(LocalDateTime.now());
        user.setBalance(user.getBalance() + project.getCollected());
        project.setCollected(0);
        projectRepository.save(project);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString("Проект успешно закрыт"));
    }

}
