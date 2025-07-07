package pl.doleckijakub.geet.controller;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.doleckijakub.geet.command.GitCommand;
import pl.doleckijakub.geet.model.Repo;
import pl.doleckijakub.geet.model.RepoVisibility;
import pl.doleckijakub.geet.model.User;
import pl.doleckijakub.geet.repository.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/repo")
public class RepoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepoController.class);
    private static final String REPO_NAME_REGEX = "[a-zA-Z0-9_.-]+";

    private final UserRepository userRepository;
    private final RepoRepository repoRepository;
    private final RepoVisibilityRepository repoVisibilityRepository;

    public RepoController(UserRepository userRepository, RepoRepository repoRepository, RepoVisibilityRepository repoVisibilityRepository) {
        this.userRepository = userRepository;
        this.repoRepository = repoRepository;
        this.repoVisibilityRepository = repoVisibilityRepository;
    }

    private static boolean isValidRepoName(String name) {
        return name != null && name.matches(REPO_NAME_REGEX);
    }

    private static void initBareRepo(File repoLocation) throws IOException, InterruptedException {
        if (repoLocation.exists()) {
            throw new IOException("Repository directory already exists: " + repoLocation.getAbsolutePath());
        }

        if (!repoLocation.mkdirs()) {
            throw new IOException("Failed to create repository directory: " + repoLocation.getAbsolutePath());
        }

        GitCommand command = new GitCommand(repoLocation, "init", "--bare");
        command.start();
        command.waitFor();

        File headFile = new File(repoLocation, "HEAD");
        if (!headFile.exists()) {
            throw new IOException("git init --bare did not create HEAD file, repository may be invalid");
        }
    }

    @PutMapping
    public ResponseEntity<Map<String, ?>> create(HttpSession session, @RequestBody Map<String, String> body) {
        String username = (String) session.getAttribute("user");

        if (username == null) return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Not logged in"
        ));

        User user = userRepository.findByUsername(username).get();

        String name = body.get("name");
        String s_visibility = body.get("visibility");
        RepoVisibility visibility = null;

        if (
                name == null
                        || s_visibility == null
                        || (visibility = repoVisibilityRepository.findByName(s_visibility).get()) == null
        ) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Invalid input"
            ));
        }

        if (name.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Repository name cannot be empty"
            ));
        }

        if (!isValidRepoName(name)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Repository name can only contain letters, digits, and the characters _, ., and -."
            ));
        }

        if (name.endsWith(".git")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Repository name cannot end with .git"
            ));
        }

        if (repoRepository.findByUserAndName(user, name).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Repository " + name + " already exists under your account"
            ));
        }

        Repo newRepo = new Repo(user, name, visibility);

        try {
            initBareRepo(newRepo.getRepoLocation());
            repoRepository.save(newRepo);
        } catch (Exception e) {
            LOGGER.error("Failed to create repository @{}/{}: {}", user.getUsername(), name, e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "Failed to create the repository"
            ));
        }

        LOGGER.info("A {} repository @{}/{} created", visibility.getName(), user.getUsername(), name);

        return ResponseEntity.ok().body(Map.of(
                "success", true
        ));
    }

    @GetMapping
    public ResponseEntity<Map<String, ?>> get(@RequestBody Map<String, String> body) {
        Map<String, Object> responseBody = new HashMap<>();

        responseBody.put("success", true);

        List<String> publicRepos = repoRepository
                .findAll()
                .stream()
                .filter(repo -> repo.getVisibility().getName().equals("public"))
                .map(repo -> String.format(
                        Locale.ENGLISH,
                        "@%s/%s",
                        repo.getUser().getUsername(),
                        repo.getName()
                ))
                .toList();

        responseBody.put("repositories", publicRepos);

        return ResponseEntity.ok().body(responseBody);
    }
}
