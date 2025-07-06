package pl.doleckijakub.geet.controller;

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
@RequestMapping("/api/account")
public class AccountController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);
    private static final String USERNAME_REGEX = "[a-zA-Z0-9_.-]+";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountController(UserRepository repo, PasswordEncoder enc) {
        this.userRepository = repo;
        this.passwordEncoder = enc;
    }

    private static boolean isValidUserame(String username) {
        return username != null && username.matches(USERNAME_REGEX);
    }

    @PutMapping
    public ResponseEntity<Map<String, ?>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null || username.isBlank() || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Invalid credentials"
            ));
        }

        if (!isValidUserame(username)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Username can only contain letters, digits, and the characters _, ., and -."
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

        LOGGER.info("User {} registered with id {}", newUser.getUsername(), newUser.getId());

        return ResponseEntity.ok(Map.of("success", true));
    }
}