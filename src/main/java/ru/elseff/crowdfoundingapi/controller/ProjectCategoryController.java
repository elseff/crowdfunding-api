package ru.elseff.crowdfoundingapi.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.elseff.crowdfoundingapi.dao.repository.ProjectCategoryRepository;
import ru.elseff.crowdfoundingapi.dto.ProjectCategoryDto;

import java.util.List;

@Slf4j
@Validated
@Transactional
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectCategoryController {
    ProjectCategoryRepository projectCategoryRepository;

    @GetMapping
    public List<ProjectCategoryDto> findAll() {
        return projectCategoryRepository.findAll()
                .stream()
                .map(c -> ProjectCategoryDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .toList();
    }
}
