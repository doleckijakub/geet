package pl.doleckijakub.geet.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import pl.doleckijakub.geet.model.Repo;
import pl.doleckijakub.geet.model.User;
import pl.doleckijakub.geet.repository.RepoRepository;
import pl.doleckijakub.geet.repository.UserRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@RestController
public class GitController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitController.class);

    private final UserRepository userRepository;
    private final RepoRepository repoRepository;

    public GitController(UserRepository userRepository, RepoRepository repoRepository) {
        this.userRepository = userRepository;
        this.repoRepository = repoRepository;
    }

    private Optional<Repo> getRepo(String username, String repoName) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return Optional.empty();

        User user = userOpt.get();

        return repoRepository.findByUserAndName(user, repoName);
    }

    @RequestMapping(
            value = {
                    "/@{username}/{repoName}.git/info/refs",
                    "/@{username}/{repoName}/info/refs",
            },
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> handleInfoRefs(
            @PathVariable String username,
            @PathVariable String repoName,
            @RequestParam(name = "service", required = false) String service
    ) throws IOException {
        if (!service.equals("git-upload-pack")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported service".getBytes());
        }

        Optional<Repo> repoOpt = getRepo(username, repoName);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Repo repo = repoOpt.get();
        if (!repo.getVisibility().getName().equals("public")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        File repoPath = repo.getRepoLocation();

        ProcessBuilder pb = new ProcessBuilder("git", "upload-pack", "--stateless-rpc", "--advertise-refs", ".");
        pb.directory(repoPath);
        Process process = pb.start();
        byte[] gitOutput = StreamUtils.copyToByteArray(process.getInputStream());

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write("001e# service=git-upload-pack\n".getBytes());
        out.write("0000".getBytes());
        out.write(gitOutput);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-git-upload-pack-advertisement"));
        headers.set("Cache-Control", "no-cache");

        return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
    }

    @RequestMapping(
            value = {
                    "/@{username}/{repoName}.git/**",
                    "/@{username}/{repoName}/**"
            },
            method = {RequestMethod.GET, RequestMethod.POST}
    )
    public ResponseEntity<byte[]> handleGitFile(
            @PathVariable String username,
            @PathVariable String repoName,
            HttpServletRequest request
    ) throws IOException {
        LOGGER.debug("{} {}", request.getMethod(), request.getRequestURI());

        Optional<Repo> repoOpt = getRepo(username, repoName);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Repo repo = repoOpt.get();

        if (!repo.getVisibility().getName().equals("public")) { // TODO: handle non-public repos
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        String fullPath = request.getRequestURI().replaceFirst("^/@" + username + "/" + repoName + "(\\.git)?", "");
        File repoBase = repo.getRepoLocation();
        File targetFile = new File(repoBase, fullPath);

        if (!targetFile.getCanonicalPath().startsWith(repoBase.getCanonicalPath())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if (!targetFile.exists() || !targetFile.isFile()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        LOGGER.info("{} {}", request.getMethod(), request.getRequestURI());

        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            contentType = MediaType.parseMediaType(Files.probeContentType(targetFile.toPath()));
        } catch (Exception ignored) {}

        byte[] data = StreamUtils.copyToByteArray(new FileSystemResource(targetFile).getInputStream());

        return ResponseEntity.ok()
                .contentType(contentType)
                .contentLength(data.length)
                .body(data);
    }
}
