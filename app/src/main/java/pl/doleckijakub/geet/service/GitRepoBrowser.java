package pl.doleckijakub.geet.service;

import jakarta.annotation.Nullable;
import pl.doleckijakub.geet.command.GitCommand;
import pl.doleckijakub.geet.model.Repo;
import pl.doleckijakub.geet.model.RepoEntry;
import pl.doleckijakub.geet.model.RepoEntryType;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GitRepoBrowser {

    public static List<RepoEntry> getEntries(Repo repo, String treeish, @Nullable String path) throws IOException, InterruptedException {
        List<RepoEntry> entries = new ArrayList<>();
        File repoDir = repo.getRepoLocation();

        path = path == null ? "." : (path + "/");

        GitCommand lsTree = new GitCommand(repoDir, "ls-tree", treeish, path);
        lsTree.start();
        byte[] output = lsTree.readAllStdout();
        lsTree.waitFor();

        String[] lines = new String(output).split("\n");
        for (String line : lines) {
            if (line.isBlank()) continue;
            entries.add(repoEntryFromTreeLine(repo, treeish, path, line));
        }

        if (entries.isEmpty()) throw new NoSuchFileException(path);

        return entries;
    }

    private static RepoEntry repoEntryFromTreeLine(Repo repo, String treeish, String path, String line) throws IOException, InterruptedException {
        // Format: <mode> <type> <sha>\t<name>
        String[] parts = line.split("\t", 2);
        String meta = parts[0];
        String name = parts[1];

        String[] metaParts = meta.split(" ");
        String modeStr = metaParts[0];
        String typeStr = metaParts[1];
        String shaStr = metaParts[2];

        RepoEntryType type;
        if ("120000".equals(modeStr)) {
            type = RepoEntryType.LINK;
        } else {
            type = switch (typeStr) {
                case "blob" -> RepoEntryType.FILE;
                case "tree" -> RepoEntryType.DIRECTORY;
                case "commit" -> RepoEntryType.SUBMODULE;
                default -> throw new IllegalArgumentException("Unknown type: " + typeStr);
            };
        }

        String fullPath = switch (type) {
            case FILE, LINK -> name;
            case DIRECTORY -> path + "/";
            case SUBMODULE -> throw new RemoteException("Unimplemented");
        };

        if (!path.equals(".")) name = name.substring(path.length());

        GitCommand command = new GitCommand(repo.getRepoLocation(), "log", "-1", "--format=%H,%ct", treeish, "--", fullPath);
        command.start();

        byte[] logOut = command.readAllStdout();
        command.waitFor();

        String logLine = new String(logOut).trim();
        String[] logParts = logLine.split(",");
        String commitShaStr = logParts[0];
        long unixTimestamp = Long.parseLong(logParts[1]);

        Timestamp ts = new Timestamp(unixTimestamp * 1000);

        return new RepoEntry(name, commitShaStr, ts, type);
    }
}