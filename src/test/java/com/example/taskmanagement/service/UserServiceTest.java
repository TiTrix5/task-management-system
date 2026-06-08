package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.LoginRequest;
import com.example.taskmanagement.dto.request.RegisterRequest;
import com.example.taskmanagement.dto.response.JwtResponse;
import com.example.taskmanagement.dto.response.UserResponse;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.BadRequestException;
import com.example.taskmanagement.mapper.UserMapper;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.security.UserDetailsImpl;
import com.example.taskmanagement.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encoded")
                .roles(Set.of("ROLE_USER"))
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest req = new RegisterRequest("newuser", "new@example.com", "password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any())).thenReturn(UserResponse.builder()
                .id(testUser.getId()).username("testuser").email("test@example.com").roles(Set.of("ROLE_USER")).build());

        UserResponse res = userService.register(req);

        assertEquals("testuser", res.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameTaken() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        assertThrows(BadRequestException.class,
                () -> userService.register(new RegisterRequest("testuser", "x@x.com", "123456")));
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest("testuser", "password123");
        Authentication auth = mock(Authentication.class);
        UserDetailsImpl ud = new UserDetailsImpl(testUser.getId(), "testuser", "test@example.com", "enc", null);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(ud);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("jwt-token");

        JwtResponse res = userService.login(req);

        assertEquals("jwt-token", res.getToken());
        assertEquals(testUser.getId(), res.getUserId());
    }
}