package pl.doleckijakub.geet.model;

import java.sql.Timestamp;

public class RepoEntry {
    private String name;
    private String lastCommitShaStr;
    private Timestamp updatedAt;
    private RepoEntryType type;

    public RepoEntry(String name, String lastCommitShaStr, Timestamp updatedAt, RepoEntryType type) {
        this.name = name;
        this.lastCommitShaStr = lastCommitShaStr;
        this.updatedAt = updatedAt;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getLastCommitShaStr() {
        return lastCommitShaStr;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public RepoEntryType getType() {
        return type;
    }
}
