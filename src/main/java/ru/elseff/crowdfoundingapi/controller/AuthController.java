package ru.elseff.crowdfoundingapi.controller;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.elseff.crowdfoundingapi.dao.entity.User;
import ru.elseff.crowdfoundingapi.dao.repository.UserRepository;
import ru.elseff.crowdfoundingapi.dto.LoginUserRequest;
import ru.elseff.crowdfoundingapi.dto.RegisterUserRequest;
import ru.elseff.crowdfoundingapi.dto.RegisterUserResponse;
import ru.elseff.crowdfoundingapi.dto.UserDto;

import java.util.Optional;

@Slf4j
@Validated
@Transactional
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    UserRepository userRepository;

    @PostMapping("/register")
    public RegisterUserResponse register(@RequestBody @Valid RegisterUserRequest request) {
        try {
            InternetAddress address = new InternetAddress(request.getEmail());
            address.validate();
        } catch (AddressException e) {
            String message = String.format("Email %s is not valid", request.getEmail());
            log.error(message);
            throw new IllegalArgumentException(message);
        }


        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional
                .isPresent()) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }


        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        user = userRepository.save(user);

        return RegisterUserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .balance(user.getBalance())
                .message("Пользователь успешно зарегистрирован")
                .build();
    }

    @PostMapping("/login")
    public UserDto login(@RequestBody @Valid LoginUserRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with email" + request.getEmail() + "doesn't exists");
        }
        User user = userOptional.get();
        if (user.getPassword().equals(request.getPassword())) {
            return UserDto.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .build();
        } else {
            throw new IllegalArgumentException("incorrect password");
        }
    }
}
