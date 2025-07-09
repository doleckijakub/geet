package pl.doleckijakub.geet.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.doleckijakub.geet.command.GitCommand;
import pl.doleckijakub.geet.model.Repo;
import pl.doleckijakub.geet.model.RepoVisibility;
import pl.doleckijakub.geet.repository.RepoRepository;
import pl.doleckijakub.geet.repository.UserRepository;
import pl.doleckijakub.geet.util.ByteStringConverter;

import java.io.*;
import java.util.Optional;

@RestController
public class GitController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitController.class);
    private static final String AUTH_TOKEN = "12345678";

    private final UserRepository userRepository;
    private final RepoRepository repoRepository;

    public GitController(UserRepository userRepository, RepoRepository repoRepository) {
        this.userRepository = userRepository;
        this.repoRepository = repoRepository;
    }

    private Optional<Repo> getRepo(String username, String repoName) {
        return userRepository.findByUsername(username)
                .flatMap(user -> repoRepository.findByUserAndName(user, repoName));
    }

    private boolean isAuthorized(String header) {
        if (header == null || !header.startsWith("Basic ")) return false;
        String base64 = header.substring(6).trim();
        String decoded = new String(ByteStringConverter.fromBase64(base64));
        // expected: "anyusername:12345678"
        return decoded.endsWith(":" + AUTH_TOKEN);
    }

    @GetMapping({
            "/@{username}/{repoName}.git/info/refs",
            "/@{username}/{repoName}/info/refs"
    })
    public ResponseEntity<byte[]> getGitInfoRefs(
            @PathVariable String username,
            @PathVariable String repoName,
            @RequestParam(name = "service", required = false) String service,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) throws Exception {
        if (service == null || !service.startsWith("git-")) {
            return ResponseEntity.badRequest().build();
        }

        String serviceName = service.substring(4);

        Optional<Repo> repoOpt = getRepo(username, repoName);
        if (repoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Repo repo = repoOpt.get();

        if (repo.getVisibility() != RepoVisibility.PUBLIC && !isAuthorized(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        GitCommand command = new GitCommand(
                repo.getRepoLocation(),
                serviceName, "--stateless-rpc", "--advertise-refs", "."
        );

        command.start();
        command.waitFor();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        {
            String firstLine = String.format("# service=git-%s\n0000", serviceName);
            firstLine = String.format("%04x%s", firstLine.length(), firstLine);
            out.write(firstLine.getBytes());
        }
        out.write(command.readAllStdout());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(String.format("application/x-git-%s-advertisement", serviceName)));
        headers.set("Cache-Control", "no-cache");

        return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
    }

    @PostMapping({
            "/@{username}/{repoName}.git/git-receive-pack",
            "/@{username}/{repoName}/git-receive-pack"
    })
    public ResponseEntity<byte[]> postGitReceivePack(
            @PathVariable String username,
            @PathVariable String repoName,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody byte[] requestBody
    ) throws IOException, InterruptedException {
        Optional<Repo> repoOpt = getRepo(username, repoName);
        if (repoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Repo repo = repoOpt.get();

        if (repo.getVisibility() != RepoVisibility.PUBLIC && !isAuthorized(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        GitCommand command = new GitCommand(
                repo.getRepoLocation(),
                "receive-pack", "--stateless-rpc", "."
        );

        command.start();

        command.writeToStdin(requestBody);
        command.closeStdin();

        byte[] responseBytes = command.readAllStdout();

        int exitCode = command.waitFor();
        if (exitCode != 0) {
            LOGGER.error("'{}' exited with exit code {}: {}", command, exitCode, "ERROR"); // TODO: read error from stderr
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-git-receive-pack-result"));
        headers.set("Cache-Control", "no-cache");

        return new ResponseEntity<>(responseBytes, headers, HttpStatus.OK);
    }

    @PostMapping({
            "/@{username}/{repoName}.git/git-upload-pack",
            "/@{username}/{repoName}/git-upload-pack"
    })
    public ResponseEntity<byte[]> postGitUploadPack(
            @PathVariable String username,
            @PathVariable String repoName,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody byte[] requestBody
    ) throws IOException, InterruptedException {
        Optional<Repo> repoOpt = getRepo(username, repoName);
        if (repoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Repo repo = repoOpt.get();

        if (repo.getVisibility() != RepoVisibility.PUBLIC && !isAuthorized(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        GitCommand command = new GitCommand(
                repo.getRepoLocation(),
                "upload-pack", "--stateless-rpc", "."
        );

        command.start();

        command.writeToStdin(requestBody);
        command.closeStdin();

        byte[] responseBytes = command.readAllStdout();

        int exitCode = command.waitFor();
        if (exitCode != 0) {
            LOGGER.error("'{}' exited with exit code {}: {}", command, exitCode, "ERROR"); // TODO: read error from stderr
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-git-receive-pack-result"));
        headers.set("Cache-Control", "no-cache");

        return new ResponseEntity<>(responseBytes, headers, HttpStatus.OK);
    }

    // TODO: uncomment or remove
//    @RequestMapping(
//            value = {
//                    "/@{username}/{repoName}.git/**",
//                    "/@{username}/{repoName}/**"
//            },
//            method = {RequestMethod.GET, RequestMethod.POST}
//    )
//    public ResponseEntity<byte[]> handleGitFile(
//            @PathVariable String username,
//            @PathVariable String repoName,
//            HttpServletRequest request,
//            @RequestHeader(value = "Authorization", required = false) String authHeader
//    ) throws IOException {
//        Optional<Repo> repoOpt = getRepo(username, repoName);
//        if (repoOpt.isEmpty()) return ResponseEntity.notFound().build();
//        Repo repo = repoOpt.get();
//
//        if (repo.getVisibility() != RepoVisibility.PUBLIC && !isAuthorized(authHeader)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        String fullPath = request.getRequestURI().replaceFirst("^/@" + username + "/" + repoName + "(\\.git)?", "");
//        File repoBase = repo.getRepoLocation();
//        File targetFile = new File(repoBase, fullPath);
//
//        if (!targetFile.getCanonicalPath().startsWith(repoBase.getCanonicalPath())) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//        }
//
//        if (!targetFile.exists() || !targetFile.isFile()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//
//        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
//        try {
//            contentType = MediaType.parseMediaType(Files.probeContentType(targetFile.toPath()));
//        } catch (Exception ignored) {}
//
//        byte[] data = StreamUtils.copyToByteArray(new FileSystemResource(targetFile).getInputStream());
//
//        return ResponseEntity.ok()
//                .contentType(contentType)
//                .contentLength(data.length)
//                .body(data);
//    }
}
