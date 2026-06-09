package org.example.counterparty.service;

import org.example.counterparty.entity.User;
import org.example.counterparty.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для UserService")
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;


    @Test
    @DisplayName("Должен успешно зарегистрировать нового польозователя")
    void shouldRegisterNewUser() {
        String username = "newuser";
        String rawPassword = "password123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        User result = userService.register(username, rawPassword);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isNotEqualTo(rawPassword);
        assertThat(result.getRole()).isEqualTo("USER");

        verify(userRepository).existsByUsername(username);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение при регистрации существующего пользователя")
    void shouldThrowExceptionWhenUserAlreadyExists() {
        String username = "existinguser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThatThrownBy(() -> userService.register(username, "password"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Username already exists");

        verify(userRepository).existsByUsername(username);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Должен найти пользователя по username")
    void shouldFindUserByUsername() {
        String username = "testuser";
        User existingUser = new User(username, "encodedPass", "USER");
        existingUser.setId(1L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        Optional<User> result = userService.findByUsername(username);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
        assertThat(result.get().getId()).isEqualTo(1L);

        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional, если пользователь не найден")
    void shouldReturnEmptyOptionalWhenUserNotFound() {
        String username = "unknown";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername(username);

        assertThat(result).isEmpty();
        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Должен найти пользователя по ID")
    void shouldFindUserById() {
        Long userId = 1L;
        User existingUser = new User("testuser", "encodedPass", "USER");
        existingUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        User result = userService.findById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Должен вернуть null, если пользователь по ID не найден")
    void shouldReturnNullWhenUserNotFoundById() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        User result = userService.findById(userId);

        assertThat(result).isNull();
        verify(userRepository).findById(userId);
    }

}
