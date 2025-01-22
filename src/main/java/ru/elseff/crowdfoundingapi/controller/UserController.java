package ru.elseff.crowdfoundingapi.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.elseff.crowdfoundingapi.dao.entity.User;
import ru.elseff.crowdfoundingapi.dao.repository.UserRepository;
import ru.elseff.crowdfoundingapi.dto.UserDto;

import java.util.List;

@Slf4j
@Validated
@Transactional
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserRepository userRepository;

    @GetMapping
    public List<UserDto> findAll() {
        return userRepository
                .findAll()
                .stream()
                .map(u -> UserDto.builder()
                        .id(u.getId())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .email(u.getEmail())
                        .build())
                .toList();
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .balance(user.getBalance())
                .build();
    }

}
