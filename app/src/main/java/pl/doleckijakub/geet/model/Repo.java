package pl.doleckijakub.geet.model;

import jakarta.persistence.*;

import java.io.File;
import java.io.Serializable;

@Entity
@Table(name = "repos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "name"})
})
public class Repo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private RepoVisibility visibility;

    public Repo() {}

    public Repo(User user, String name, RepoVisibility visibility) {
        this.user = user;
        this.name = name;
        this.visibility = visibility;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public RepoVisibility getVisibility() {
        return visibility;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVisibility(RepoVisibility visibility) {
        this.visibility = visibility;
    }

    ///

    public File getRepoLocation() {
        return new File(getUser().getReposLocation(), getName() + ".git");
    }

    public String getDefaultBranchName() {
        return "master";
    }
}