package pl.doleckijakub.geet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.doleckijakub.geet.model.Repo;
import pl.doleckijakub.geet.model.User;

import java.util.Optional;

public interface RepoRepository extends JpaRepository<Repo, Long> {
    Optional<Repo> findByUserAndName(User user, String name);
}