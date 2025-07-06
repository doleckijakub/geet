package pl.doleckijakub.geet.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "repo_visibilities")
public class RepoVisibility implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true)
    private String name;

    public RepoVisibility() {}

    public RepoVisibility(String name) {
        this.name = name;
    }

    public Short getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
