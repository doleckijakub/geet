package pl.doleckijakub.geet.command;

import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class GitCommand {

    private final File directory;
    private final List<String> args;
    private Process process;

    public GitCommand(File repoLocation, String... args) {
        this.directory = repoLocation;
        this.args = new ArrayList<>(List.of("git"));
        this.args.addAll(List.of(args));
    }

    public void start() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(directory);
        builder.redirectErrorStream(false);
        process = builder.start();
    }

    public void writeToStdin(byte[] input) throws IOException {
        try (OutputStream stdin = process.getOutputStream()) {
            stdin.write(input);
            stdin.flush();
        }
    }

    public void closeStdin() throws IOException {
        process.getOutputStream().close();
    }

    public byte[] readAllStdout() throws IOException {
        return process.getInputStream().readAllBytes();
    }

    public byte[] readAllStderr() throws IOException {
        return process.getErrorStream().readAllBytes();
    }

    public int waitFor() throws InterruptedException {
        return process.waitFor();
    }
}
