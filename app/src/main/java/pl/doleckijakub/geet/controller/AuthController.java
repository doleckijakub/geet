package pl.doleckijakub.geet.controller;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.websocket.servlet.UndertowWebSocketServletWebServerCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.doleckijakub.geet.model.User;
import pl.doleckijakub.geet.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<Map<String, ?>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null || username.isBlank() || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Invalid credentials"
            ));
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "error", "Username taken"
            ));
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setAdmin(false);

        userRepository.save(newUser);

        logger.info("User {} registered with id {}", newUser.getUsername(), newUser.getId());

        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, ?>> login(@RequestBody Map<String, String> login, HttpSession session) {
        String username = login.get("username");
        String password = login.get("password");

        if (username == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "error", "Username or password not provided"
            ));
        }

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty() || !passwordEncoder.matches(password, user.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "error", "Invalid credentials"
            ));
        }

        String db_username = user.get().getUsername();

        session.setAttribute("user", db_username);

        logger.info("User {} logged in", db_username);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "username", db_username
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, ?>> logout(HttpSession session) {
        String username = (String) session.getAttribute("user");

        if (username == null) return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Not logged in"
        ));

        logger.info("User {} logged out", username);

        session.invalidate();

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/auth-status")
    public ResponseEntity<Map<String, ?>> status(HttpSession session) {
        String username = (String) session.getAttribute("user");

        if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "error", "Not logged in"
        ));

        return ResponseEntity.ok(Map.of(
                "success", true,
                "username", username
        ));
    }
}