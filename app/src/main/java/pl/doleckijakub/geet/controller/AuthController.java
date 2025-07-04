package pl.doleckijakub.geet.controller;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.doleckijakub.geet.model.User;
import pl.doleckijakub.geet.repository.UserRepository;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository repo, PasswordEncoder enc) {
        this.userRepository = repo;
        this.passwordEncoder = enc;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null || username.isBlank() || password.length() < 6) {
            return ResponseEntity.badRequest().body("Invalid input");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username taken");
        }

        var newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setAdmin(false);

        userRepository.save(newUser);

        logger.info("User {{ id: {}, username: {} }} registered", newUser.getId(), newUser.getUsername());

        return ResponseEntity.ok("Registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> login,
                                   HttpSession session) {
        var user = userRepository.findByUsername(login.get("username"));
        if (user.isEmpty() || !passwordEncoder.matches(login.get("password"), user.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        session.setAttribute("user", user.get().getUsername());

        logger.info("User {{ id: {}, username: {} }} logged in", user.get().getId(), user.get().getUsername());

        return ResponseEntity.ok("Logged in");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String username = (String) session.getAttribute("user");

        logger.info("User {{ username: {} }} logged out", username);

        session.invalidate();

        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/auth-status")
    public ResponseEntity<?> status(HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username != null) return ResponseEntity.ok(Map.of("username", username));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
    }
}