package pl.doleckijakub.geet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.doleckijakub.geet.model.RepoVisibility;

import java.util.Optional;

public interface RepoVisibilityRepository extends JpaRepository<RepoVisibility, Long> {
    Optional<RepoVisibility> findByName(String name);
}