package pl.doleckijakub.geet.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import pl.doleckijakub.geet.command.GitCommand;
import pl.doleckijakub.geet.model.Repo;
import pl.doleckijakub.geet.model.RepoEntry;
import pl.doleckijakub.geet.model.RepoVisibility;
import pl.doleckijakub.geet.model.User;
import pl.doleckijakub.geet.repository.*;
import pl.doleckijakub.geet.service.GitRepoBrowser;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.*;

@RestController
@RequestMapping("/api/repo")
public class RepoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepoController.class);
    private static final String REPO_NAME_REGEX = "[a-zA-Z0-9_.-]+";

    private final UserRepository userRepository;
    private final RepoRepository repoRepository;

    public RepoController(UserRepository userRepository, RepoRepository repoRepository) {
        this.userRepository = userRepository;
        this.repoRepository = repoRepository;
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

        User user = userRepository.findByUsername(username).get(); // TODO: handle empty

        String name = body.get("name");
        String s_visibility = body.get("visibility");
        RepoVisibility visibility = null;

        if (
                name == null
                        || s_visibility == null
                        || (visibility = RepoVisibility.fromString(s_visibility)) == null
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

        LOGGER.info("A {} repository @{}/{} created", visibility.toString(), user.getUsername(), name);

        return ResponseEntity.ok().body(Map.of(
                "success", true
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, ?>> search(@RequestBody Map<String, String> body) {
        // TODO: well... search

        Map<String, Object> responseBody = new HashMap<>();

        responseBody.put("success", true);

        List<String> publicRepos = repoRepository
                .findAll()
                .stream()
                .filter(repo -> repo.getVisibility() == RepoVisibility.PUBLIC)
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

    @GetMapping({
            "/{username}/{repoName}",
            "/{username}/{repoName}/{branch}",
            "/{username}/{repoName}/{branch}/**",
    })
    public ResponseEntity<?> repoData(
            @PathVariable String username,
            @PathVariable String repoName,
            @PathVariable(required = false) String branch,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request
    ) throws IOException, InterruptedException {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();

        Optional<Repo> repoOpt = repoRepository.findByUserAndName(user, repoName);
        if (repoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Repo repo = repoOpt.get();

//        if (repo.getVisibility() != RepoVisibility.PUBLIC) { // TODO: implement repo.canView(request) and repo.canPush(request)
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }

        if (branch == null) branch = repo.getDefaultBranchName();

        String pattern = String.format("/api/repo/%s/%s/%s/**", username, repoName, branch);
        String path = new AntPathMatcher().extractPathWithinPattern(pattern, request.getRequestURI());

        if (path.isEmpty()) path = null;

        try {
            List<RepoEntry> entries = GitRepoBrowser.getEntries(repo, branch, path);

            class Response {
                public final String owner;
                public final String name;
                public final RepoVisibility visibility;
                public final String defaultBranch;
                public final List<RepoEntry> entries;

                Response(String owner, String name, RepoVisibility visibility, String defaultBranch, List<RepoEntry> entries) {
                    this.owner = owner;
                    this.name = name;
                    this.visibility = visibility;
                    this.defaultBranch = defaultBranch;
                    this.entries = entries;
                }
            }

            return ResponseEntity.ok().body(new Response(
                    repo.getUser().getUsername(),
                    repo.getName(),
                    repo.getVisibility(),
                    repo.getDefaultBranchName(),
                    entries
            ));
        } catch (NoSuchFileException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "No such file"
            ));
        }
    }
}
