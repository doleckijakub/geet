package pl.doleckijakub.geet.model;

import jakarta.persistence.*;
import pl.doleckijakub.geet.config.GitConfig;

import java.io.File;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    ///

    public File getReposLocation() {
        return new File(GitConfig.REPO_LOCATION_BASE, getUsername());
    }
}