package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ChangePasswordRequest;
import com.example.usermanagement.dto.UpdateProfileRequest;
import com.example.usermanagement.dto.UserProfileResponse;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ GET PROFILE
    @GetMapping("/profile")
    public UserProfileResponse getProfile() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserProfileResponse(
    user.getId(),
    user.getName(),
    user.getEmail(),
    user.getRole()
);

    }

    // ✅ UPDATE PROFILE
    @PutMapping("/update-profile")
public ResponseEntity<UserProfileResponse> updateProfile(
        @RequestBody UpdateProfileRequest request) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    user.setName(request.getName());
    userRepository.save(user);

    UserProfileResponse response = new UserProfileResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole()   // ✅ DO NOT USE .name()
    );

    return ResponseEntity.ok(response);
}


    // ✅ CHANGE PASSWORD
    @PutMapping("/change-password")
public ResponseEntity<String> changePassword(
        @RequestBody ChangePasswordRequest request) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // ✅ CHECK OLD PASSWORD CORRECTLY
    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
        return ResponseEntity.badRequest().body("Old password incorrect");
    }

    // ✅ ENCODE NEW PASSWORD
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    return ResponseEntity.ok("Password changed successfully");
}


    // ✅ DELETE ACCOUNT
    @DeleteMapping("/delete-account")
public ResponseEntity<String> deleteAccount(Authentication authentication) {

    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    userRepository.delete(user);

    return ResponseEntity.ok("Account deleted successfully");
}

}
