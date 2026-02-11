package com.example.usermanagement.controller;

import com.example.usermanagement.config.JwtUtil;
import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.RegisterRequest;
import com.example.usermanagement.entity.BlacklistedToken;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.repository.BlacklistedTokenRepository;
import com.example.usermanagement.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            BlacklistedTokenRepository blacklistedTokenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    // ✅ REGISTER
    @PostMapping("/register")
public ResponseEntity<String> register(@RequestBody RegisterRequest request) {

    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
        return ResponseEntity.badRequest().body("Email already registered");
    }

    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));

    // ✅ FIX: If role not selected → default USER
    if (request.getRole() == null || request.getRole().isEmpty()) {
        user.setRole("USER");
    } else {
        user.setRole(request.getRole().toUpperCase());
    }

    userRepository.save(user);

    return ResponseEntity.ok("User registered successfully");
}


    // ✅ LOGIN (JWT)
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody LoginRequest request
    ) {

        Optional<User> userOpt =
                userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Invalid email or password");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            return ResponseEntity.badRequest()
                    .body("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return ResponseEntity.ok(token);
    }

    // ✅ LOGOUT (REAL-WORLD)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setToken(token);

            blacklistedTokenRepository.save(blacklistedToken);
        }

        return ResponseEntity.ok("Logged out successfully");
    }
}
