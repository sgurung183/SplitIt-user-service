package com.SplitIt.user_service.service;

import com.SplitIt.user_service.dto.LoginRequest;
import com.SplitIt.user_service.dto.LoginResponse;
import com.SplitIt.user_service.dto.RegisterRequest;
import com.SplitIt.user_service.dto.RegisterResponse;
import com.SplitIt.user_service.entity.User;
import com.SplitIt.user_service.exception.DuplicateEmailException;
import com.SplitIt.user_service.exception.DuplicatePhoneNumberException;
import com.SplitIt.user_service.exception.InvalidCredentialsException;
import com.SplitIt.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_whenEmailAlreadyExists_throwsDuplicateEmailException() {
        RegisterRequest request = validRegisterRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenPhoneNumberAlreadyExists_throwsDuplicatePhoneNumberException() {
        RegisterRequest request = validRegisterRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicatePhoneNumberException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenValid_hashesPasswordBeforeSaving() {
        RegisterRequest request = validRegisterRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        RegisterResponse response = userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getPasswordHash()).isNotEqualTo(request.getPassword());
        assertThat(passwordEncoder.matches(request.getPassword(), savedUser.getPasswordHash())).isTrue();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
    }

    @Test
    void login_whenEmailNotFound_throwsInvalidCredentialsException() {
        LoginRequest request = loginRequest("missing@example.com", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(jwtService);
    }

    @Test
    void login_whenPasswordDoesNotMatch_throwsInvalidCredentialsException() {
        LoginRequest request = loginRequest("jane@example.com", "wrongPassword");
        User existingUser = User.builder()
                .id(1L)
                .email("jane@example.com")
                .passwordHash(passwordEncoder.encode("correctPassword"))
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(jwtService);
    }

    @Test
    void login_whenValid_returnsTokenFromJwtService() {
        LoginRequest request = loginRequest("jane@example.com", "correctPassword");
        User existingUser = User.builder()
                .id(1L)
                .email("jane@example.com")
                .passwordHash(passwordEncoder.encode("correctPassword"))
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(1L, "jane@example.com")).thenReturn("signed-jwt-token");

        LoginResponse response = userService.login(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getToken()).isEqualTo("signed-jwt-token");
    }

    private RegisterRequest validRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");
        request.setPhoneNumber("5551234567");
        request.setPassword("password123");
        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
